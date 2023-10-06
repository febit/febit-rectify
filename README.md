# febit-rectify

Transform raw data (json, access log, csv, custom structured log, etc.) to structured record, with schema.

## Features

+ JS Style & Java Friendly expression, using [febit-wit](https://github.com/febit/wit) as script engine
+ Free input, by defining your own `SourceFormat`
+ Free output, by defining your own `StructSpec`
+ Clean and free runtime environment, all internal methods prefixed by `$$_`, you can register your own methods
  by `EnginePlugin` (SPI)

## Usage

```java
// `$` is input, can be used in global-filter, global-code (except const statement), column expressions
// `$$` is current column value, can be used in column check expression
var conf = RectifierConf.conf()
    // Named your schema
    .name("Demo")
    // Global code
    .globalCode(""
            + "var isTruly = obj -> {\n"
            + "   return obj == true \n"
            + "              || obj == \"on\" || obj == \"true\"\n"
            + "              || obj == 1;\n"
            + "};")
    // Global filters:
    //    Notice: only a Boolean.FALSE or a non-null String (reason) can ban current row, others pass.
    .globalFilter("$.status > 0")
    //    Recommend: give a reason if falsely, `||` is logic OR (just what it means to in JS, feel free!).
    .globalFilter("$.status < 100 || \"status should <100\"")
    // Global code and filters, Will be executed in defined order.
    .globalCode("var isEven = $.status % 2 == 0 ")
    .globalCode("var statusCopy = $.status")
    .globalFilter("isEven || \"status is not even\"")
    // Columns
    .column("long", "id", "$.id")
    // column with check expression
    .column("boolean", "enable", "", "$$ || \"enable is falsely\"")
    .column("int", "status", "$.status")
    .column("boolean", "isEven", "isEven")
    .column("boolean", "call_isTruly", "isTruly($.isTrulyArg)")
    .column("string", "content", "\"prefix:\"+$.content");

Rectifier<String, Map<String, Object>> rectifier = conf.build(new JsonSourceFormat());

rectifier.process(json, consumer);
```

## License

febit-rectify is distributed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
