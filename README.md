# Febit Rectify

[![Apache-2.0 License](https://img.shields.io/badge/license-apache-blue.svg)][license]

Schema-driven data rectification for Java, Flink, and SQL workflows.

Transform raw data (JSON, access log, CSV, custom structured text, and more) into structured records with a small set
of filters and expressions.

Write the rules once, then reuse them across Java code, Flink jobs, and SQL-driven pipelines.

The typical flow is:

- **Parse**: Convert raw input into a structured object (for example JSON, CSV, or a log line).
- **Filter**: Apply boolean expressions to drop invalid records.
- **Expression Mapping**: Compute derived fields with expressions and map them to output columns.
- **Structured Output**: Emit typed records (e.g., Java objects, Flink `Row`, SQL rows).

## Why Use It

- Keep transformation rules close to the schema.
- Filter, validate, and reshape records in one place.
- Write Java-friendly, JS-style expressions powered by [febit-wit](https://github.com/febit/wit).
- Reuse the same style in plain Java, Flink streaming, Flink SQL, and SQLLine / Calcite.

In short, you describe **what the output should look like**, and Rectify handles parsing, filtering, and field mapping.

## Getting Started

Start with the smallest example that matches your runtime.

- Start with **`rectify-core`** if you want to embed rectification in Java code.
- Use **`rectify-flink`** if you already have a Flink DataStream or Flink SQL pipeline.
- Use **`rectify-sqlline`** if you want to query local files through Calcite / SQLLine.

### Core API

Best when you want to call Rectify directly from Java.

See also: [CoreExampleTest.java](rectify-core/src/test/java/org/febit/rectify/CoreExampleTest.java)

```java
var settings = RectifierSettings.builder()
    .name("QuickDemo")
    .filter("$.status > 0")
    .property("long", "id", "$.id")
    .property("int", "status", "$.status")
    .property("string", "content", "\"prefix:\" + $.content")
    .build();

var rectifier = settings.create()
    .with(new JsonSourceFormat());

rectifier.

process("""
                {"id":1,"status":10,"content":"hello"}""",
        System.out::println,
        reason ->

fail("Processing failed: "+reason)
);
```

What happens here:

- `$.status > 0` keeps only valid records
- `"prefix:" + $.content` derives a new field with a single expression

That keeps the transformation logic compact and close to the target schema.

### Flink Streaming

Best when your input is already a Flink `DataStream`.

See also: [StreamingExampleTest.java](rectify-flink/src/test/java/org/febit/rectify/flink/StreamingExampleTest.java)

```java
var settings = RectifierSettings.builder()
    .name("Demo")
    .filter("$.status > 0")
    .property("long", "id", "$.id")
    .property("boolean", "enable", "", "$$ || \"enable is falsely\"")
    .property("int", "status", "$.status")
    .property("string", "content", "\"prefix:\" + $.content")
    .build();

var env = StreamExecutionEnvironment.getExecutionEnvironment();
var source = env.fromData(
    List.of("""
        {"id":1,"enable":true,"status":10,"content":"ok"}
        {"id":2,"enable":false,"status":20,"content":"skip"}
        """.split("\n")),
    BasicTypeInfo.STRING_TYPE_INFO
);

var rows = RectifierStreamingSupport.flatMap(source, settings, new JsonSourceFormat());
rows.

print();
env.

execute("rectify-streaming-demo");
```

### Flink Table / SQL

Best when you want to define transformation rules in SQL DDL and query them with SQL.

See also: [TableExampleTest.java](rectify-flink/src/test/java/org/febit/rectify/flink/TableExampleTest.java)

```sql
CREATE
TEMPORARY TABLE input_events (
  id BIGINT,
  enable BOOLEAN,
  status INT,
  content STRING
) WITH (
  -- Replace with your connector options
  'connector' = 'filesystem',
  'path' = 'file:///path/to/whatever',

  -- febit-rectify format
  'format' = 'febit-rectifier',
  'febit-rectifier.source.format' = 'json',
  'febit-rectifier.name' = 'Demo',
  'febit-rectifier.filters' = '[''$.status > 0'', ''$.enable'']',
  'febit-rectifier.columns' = '{id: ''$.id'', status: ''$.status * 10'', content: ''"prefix:" + $.content''}'
);

SELECT id, status, content
FROM input_events
ORDER BY id DESC;
```

Options:

+ `febit-rectifier.filters` accepts a list of expressions.
+ `febit-rectifier.columns` maps output column names to expressions.

### SQLLine / Calcite

`rectify-sqlline` scans all `*.rectify.yml` files in a directory. Each file defines one table.

1. Prepare the directory structure

```text
demo/
  model.json
  tables/
    orders.log
    orders-log.rectify.yml
```

2. Create the table config at `tables/orders-log.rectify.yml`

```yaml
name: orders
path: orders.log
source: json
preinstalls:
  - var isEven = $.status % 2 == 0
properties:
  - name: id
    type: long
    expression: $.id
  - name: status
    type: int
    expression: $.status
  - name: content
    type: string
    expression: '"prefix:" + $.content'
```

3. Create the Calcite model file `model.json`

```json
{
  "version": "1.0",
  "defaultSchema": "rectify",
  "schemas": [
    {
      "name": "rectify",
      "type": "custom",
      "factory": "org.febit.rectify.sqlline.RectifySchemaFactory",
      "operand": {
        "directory": "tables"
      }
    }
  ]
}
```

4. Start SQLLine

```bash
sqlline -u "jdbc:calcite:model=/absolute/path/to/demo/model.json"
```

5. Run a query

```sql
SELECT "id", "status", "content"
FROM "orders";
```

## More Examples

### Core API: Advanced

Use this when you need preinstalled functions, multiple filters, and more advanced property expressions.

See also: [CoreExampleTest.java](rectify-core/src/test/java/org/febit/rectify/CoreExampleTest.java)

```java
// `$` is input record, can be used in filters, preinstall scripts and property expressions.
// `$$` is current property value, can be used in property check expression
var settings = RectifierSettings.builder()
        .name("Demo")
        .preinstall("""
            var isTruly = obj -> {
               return obj == true
                          || obj == "on" || obj == "true"
                          || obj == 1;
            };
            """)
        .filter("$.status > 0")
        .filter("$.status < 100 || \"status should <100\"")
        .preinstall("var isEven = $.status % 2 == 0 ")
        .preinstall("var statusCopy = $.status")
        .filter("isEven || \"status is not even\"")

        .property()
        .name("id")
        .type("long")
        .expression("$.id")
        .commit()

        .property()
        .name("enable")
        .comment("The enable property, should be true or truthy")
        .type("boolean")
        .validation("$$ || \"enable is falsely\"")
        .commit()

        .property()
        .type("string")
        .name("content")
        .expression("\"prefix:\" + $.content")
        .commit()

        .property("int", "status", null)
        .property("boolean", "isEven", "isEven")
        .property("boolean", "call_isTruly", "isTruly($.isTrulyArg)")
        .build();
```

[license]: https://github.com/febit/febit-rectify/blob/main/LICENSE.txt
