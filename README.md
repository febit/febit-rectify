# febit-rectify

Transform raw data (json, access log, csv, custom structured log, etc.) to structured record, with schema.

## Features

+ JS Style & Java Friendly expression, using [febit-wit](https://github.com/febit/wit) as script engine
+ Free input, by defining your own `SourceFormat`
+ Free output, by defining your own `StructSpec`
+ Clean and free runtime environment, all internal methods prefixed by `$$_`, you can register your own functions
  by `RectifierWitModule` (SPI)

## Usage

```java
// `$` is input record, can be used in filters, preinstall scripts and property expressions.
// `$$` is current property value, can be used in property check expression
var settings = RectifierSettings.builder()
        // Naming your schema
        .name("Demo")
        // Preinstall code or functions, which can be used in filters or property expressions.
        .preinstall("""
            var isTruly = obj -> {
               return obj == true
                          || obj == "on" || obj == "true"
                          || obj == 1;
            };
            """)
        // Global filters: false or non-null string (as reason) will ban current record.
        .filter("$.status > 0")
        //    Recommend: give a reason if falsy, `||` is logic OR (just what it means to in JS, feel free!).
        .filter("$.status < 100 || \"status should <100\"")
        .preinstall("var isEven = $.status % 2 == 0 ")
        .preinstall("var statusCopy = $.status")
        .filter("isEven || \"status is not even\"")

        // Properties:
        .property()
        .name("id")
        .type("long")
        .expression("$.id")
        .commit()

        .property()
        .name("enable")
        .comment("The enable property, should be true or truthy")
        .type("boolean")
        // If expression is not specified, the default value is `$.property`
        //   So here the default expression is `.expression("$.enable")`
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

var rectifier = settings.create()
    .with(new JsonSourceFormat());

rectifier.process(json, consumer);
```

## License

febit-rectify is distributed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
