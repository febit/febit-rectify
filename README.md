# febit-rectify

Transform raw data (json, access log, csv, custom structured log, etc.) to structured record, with schema.

## Features

+ JS Style & Java Friendly expression, use [febit-wit](https://github.com/febit/wit) as expression/script engine
+ Free input, by defining your own `SourceFormat`, `SourceFormatProvider`(SPI)
+ Free output, by defining your own `ResultModel`
+ Clean and free runtime environment, all internal methods prefixed by `$$_`, you can register your own methods by `EnginePlugin` (SPI)

## Usage

```java
// `$` is input, can be used in global-filter, global-code (except const statement), column expressions
// `$$` is current column value, can be used in column check expression
RectifierConf conf = RectifierConf.builder()
        // Named your schema
        .name("Demo")
        // Source format
        .sourceFormat("json")
        // Global code
        .addGlobalCode("const CONST_VAR = 123")
        // Global filters: 
        //    Notice: only a Boolean.FALSE or a non-null String (reason) can ban current row, others pass.
        .addGlobalFilter("$.status > 0")
        //    Recommend: give a reason if falsely, `||` is logic OR (just what it means in JS, feel free!).   
        .addGlobalFilter("$.status < 100 || \"status should <100\"")
        // Global code and filters, Will be executed in defined order.
        .addGlobalCode("var isEven = $.status % 2 == 0 ")
        .addGlobalFilter("isEven || \"status is not even\"")
        // Columns
        .addColumn("long", "id", "$.id")
        .addColumn("boolean", "isEven", "isEven")
        // column with check expression
        .addColumn("boolean", "enable", "$.enable", "$$ || \"enable is falsely\"")
        .addColumn("int", "status", "$.status")
        .addColumn("string", "content", "\"prefix:\"+$.content")
        .build();

Rectifier<String, GenericStruct> rectifier = Rectifier.create(conf);

rectifier.process(json, consumer);
```

## License

febit-rectify is distributed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
