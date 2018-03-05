components-processing
=====================

Processing components 
 

Aggregate
---------

The `Aggregate` component ...

FilterRow
---------

The `Aggregate` component ...

Normalize
---------

The `Normalize` component ...

PythonRow
---------

The `PythonRow` component ...

Replicate
---------

The `Replicate` component ...

TypeConverter
-------------

The `TypeConverter` component converts some fields from one type to another.

Each conversion is specified with three fields:
* Field (`field`): A pointer into the schema to indicate which data should be modified.
* Output type (`outputType`): The desired destination type for that data.
* Input format (`outputFormat`): Additional information used to help interpret the column (currently only the [DateFormat](https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html) and [DecimalFormat](https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html)).

More than one of these conversions can be applied.

The incoming record is [Avro](https://avro.apache.org/docs/1.8.2/spec.html), so the **source** type is guaranteed to be representable using one of the following Avro types:

* Primitives: `null`, `boolean`, `int`, `long`, `float`, `double`, `bytes`, `string`
* Complex types: `record`, `enum`, `array`, `map`, `union`, `fixed`
* Logical types: `date`:int, `time-millis`:int, `time-micros`:long, `timestamp-millis`:long, `timestamp-micros`:long, `duration`:fixed(12), `decimal`:fixed|bytes.
  * Logical types can always be safely considered as the underlying primitive/complex type.

The **destination** types were chosen to be familiar to the end user and correspond closely with the Avro types:
* Boolean, Integer, Long, Float, Double, String, Date, Time:`time-millis`, and DateTime:`timestamp-millis`.

Some of the current restrictions on this component are:

* Only primitive and some logical Avro types are supported on input.
* Unknown logical types are processed as the underlying avro type.
* The complex type `union` is supported:
  * If an source schema contains `null`, the destination value will also be a union with destination type and `null`.
  * If all of the schemas in the union are supported, the conversion is valid.
  * A `null` incoming value is always a `null` outgoing value.
* The complex type `fixed` is supported as `bytes`

Given these considerations, the following conversion table applies:

| ↓dst&nbsp;\\&nbsp;src→ | boolean | int | long | float | double | bytes | string |
| -------- | :-: | :-: | :-: | :-: | :-: | :-: | :-: |
| Boolean | `=` | !=0 | !=0 | !=0 | !=0 | !=0 | ==true
| Integer | 0/1 | `=` | :white_check_mark: | :large_orange_diamond: | :white_check_mark: | 4 bytes java | :hash: |
| Long | 0/1 | :large_orange_diamond: | `=` | :large_orange_diamond: | :large_orange_diamond: | 8 byte Java | :hash: |
| Float | 0/1 | :large_orange_diamond: | :large_orange_diamond: | `=` | :white_check_mark: | 4 byte Java | :hash: |
| Double | 0/1 | :large_orange_diamond: | :large_orange_diamond: | :large_orange_diamond: | `=` | 8 byte Java | :hash: |
| String | true/false | :hash::clock10: | :hash::clock10: | :hash: | :hash: | utf8 | `=` |
| Date | 0/1 | `=` | :white_check_mark: | :large_orange_diamond: | :white_check_mark: | (as Integer) | :clock10: |
| Time | 0/1 | `=` | :white_check_mark: | :large_orange_diamond: | :white_check_mark: | (as Integer) | :clock10: |
| DateTime | 0/1 | (as Time) :large_orange_diamond: | `=` | :large_orange_diamond: | :large_orange_diamond: | (as Long) | :clock10: |

* `=` Identity (no conversion)
* :white_check_mark: [Widening primitive conversions](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html) where no information is lost.
  * Float to double conversion assumes strictfp.
* :large_orange_diamond: [Primitive conversions](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html) (widening or narrowing) where information might be lost. :warning:
* :clock10: The `DateFormat` pattern, if present, is used for String conversions with date/time types.
  * If the source is a logical type date, time-millis, time-micros, timestamp-millis, time-micros.
  * If the dest is Date, Time or DateTime.
  * If no pattern is present, Date/Time/DateTime types use specific ISO-8601 patterns.
    * Date: `yyyy-MM-dd`
    * Time: `HH:mm:ss`
    * DateTime: `yyyy-MM-dd'T'HH:mm:ss'Z'`
* :hash: The `DecimalFormat` pattern, if present, is used for String conversions with numeric types.
  * If not present, fall back to `Integer.valueOf()` or `Integer.toString()`
   (with the appropriate destination value).


### Errors and warnings.

(Simplified version)

Warnings are not implemented, but if we were to take them into account, the following would be good candidates:
* Any :large_orange_diamond: conversion that loses information.
  * Not a warning if a pattern is specified, since it is explicit.
* Missing pattern on Date/Time/DateTime.

Errors kill the job.
* Parsing exceptions with bad `DateFormat`/`DecimalFormat` patterns,
* Any source causes exception from bad `parse` or `valueOf` conversions.
* Not enough source bytes to create a dest value.

### Future work
* Labels: Field, Convert to, Pattern.
* Refine nullables and default values.
* Use AVPath (without predicate) in field?
* Decimal type (with precision and scale, in addition to pattern).
* Complex types.
* Better error/warning handling.

Window
------

The `Window` component ...
