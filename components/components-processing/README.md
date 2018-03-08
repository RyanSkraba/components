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

[DateTimeFormatter]: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
[DecimalFormat]: https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html 
[Avro]: https://avro.apache.org/docs/1.8.2/spec.html

The `TypeConverter` component converts some field data from one type to another.

Each conversion is specified with three fields:
* Field (`field`): A pointer into the schema to indicate which data should be modified.
* Output type (`outputType`): The desired destination type for that data.
* Format (`outputFormat`): Additional information used to help interpret the column (currently only the [DateTimeFormatter][DateTimeFormatter] and [DecimalFormat][DecimalFormat]).


More than one of these conversions can be applied to an incoming record.

The incoming record is [Avro][Avro], so the **source** type is guaranteed to be representable using one of the following Avro types:

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
  * If an source schema contains `null`, the destination schema will also be a union with destination type and `null`.
  * If all of the schemas in the union are supported, the conversion is valid.
  * A `null` incoming value is always a `null` outgoing value.
* The complex type `fixed` is supported as `bytes`

Given these considerations, the following conversion table applies:

| ↓dst&nbsp;\\&nbsp;src→ | boolean | int | long | float | double | bytes | string | date | time-millis | timestamp-millis | 
| -------- | :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |
| Boolean | `=` | !=0 | !=0 | !=0 | !=0 | !=0 | ==true | !=0 | !=0 | !=0 |
| Integer | 0/1 | `=` | :large_orange_diamond: | :large_orange_diamond: | :large_orange_diamond: | 4 bytes java | :hash: | `=` | `=` | :large_orange_diamond: |
| Long | 0/1 | :white_check_mark: | `=` | :large_orange_diamond: | :large_orange_diamond: | 8 byte Java | :hash: | :white_check_mark: | :white_check_mark: | `=` |
| Float | 0/1 | :large_orange_diamond: | :large_orange_diamond: | `=` | :large_orange_diamond: | 4 byte Java | :hash: | :large_orange_diamond: | :large_orange_diamond: | :large_orange_diamond: |
| Double | 0/1 | :white_check_mark: | :large_orange_diamond: | :white_check_mark: | `=` | 8 byte Java | :hash: | :white_check_mark: | :white_check_mark: | :large_orange_diamond: |
| String | true/false | :hash: | :hash: | :hash: | :hash: | utf8 | `=` |  :clock10: |  :clock10: |  :clock10: |
| Date | 0/1 | `=` | :large_orange_diamond: | :large_orange_diamond: | :large_orange_diamond: | (as Integer) | :clock10: | `=` | always `1970-01-01` | date part only |
| Time | 0/1 | `=` | :large_orange_diamond: | :large_orange_diamond: | :large_orange_diamond: | (as Integer) | :clock10: | always `00:00:00` | `=` | time part only |
| DateTime | 0/1 | (as Time) :white_check_mark: | `=` | :large_orange_diamond: | :large_orange_diamond: | (as Long) | :clock10: | :white_check_mark: | :white_check_mark: | `=` |

* `=` Identity (no conversion)
* :white_check_mark: [Widening primitive conversions](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html) where no information is lost.
  * Float to double conversion assumes strictfp.
* :large_orange_diamond: [Primitive conversions](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html) (widening or narrowing) where information might be lost. :warning:
* :clock10: The `DateFormat` pattern, if present, is used for String conversions with date/time types.
  * If the source is a logical type `date`, `time-millis`, `timestamp-millis` (`time-micros` and `timestamp-micros` are treated as long) or the destination is Date, Time or DateTime.
  * If no pattern is present, Date/Time/DateTime types use specific ISO-8601 patterns.
    * Date: `yyyy-MM-dd`
    * Time: `HH:mm:ss`
    * DateTime: `yyyy-MM-dd'T'HH:mm:ss'Z'`
* :hash: The `DecimalFormat` pattern, if present, is used for String conversions with numeric types.
  * If not present, fall back to `Integer.valueOf()` or `Integer.toString()` (with the appropriate destination value).
* When converting between supported date-oriented types and numbers, the format isn't used.
  * Date: the incoming/outgoing number is the number of days since 1970-01-01 (`int`)
  * Time: the incoming/outgoing number is the number of milliseconds since 00:00:00 (`int`)
  * DateTime: the incoming/outgoing number is the number of milliseconds since 1970-01-01 00:00:00 (`long`)
* When the source and destination are supported date-oriented types and numbers, the date and time components are kept consistent between the two.  Anything unknown is set relative to 1970-01-01 00:00:00.
  * For example, converting a Time (with no date component) to Date will always return `1970-01-01`

### Errors and warnings.

*(NB. This will evolve as better error handling is implemented)*

Warnings are not implemented, but if we were to take them into account, the following would be good candidates:
* Any :large_orange_diamond: conversion that loses information.
  * Not a warning if a pattern is specified, since it is explicit.
* Missing pattern on Date/Time/DateTime.

**All errors kill the job.  The user is responsible for ensuring that their data is compatible with the conversion.** 
The types of errors you may encounter include:
* Parsing exceptions with bad `DateFormat`/`DecimalFormat` patterns,
* Any source causes exception from bad `parse` or `valueOf` conversions.
* Not enough source bytes to create a dest value.

Error messages should provide relevant message and include the failed input value.

### Date-oriented formats

When either the source or destination value is a date/time-oriented value **AND** the other is a string, the format is used
in the conversion, as described at [DateTimeFormatter][DateTimeFormatter].  If no format is present, the default ISO 8601
format provided with Java is used.

`DateTime` includes both calendar day and time information. The Avro date/time LogicalTypes do not include time zone
information, so this *must* be optional in the format, and will not be present in the String.  The examples below include
time zones for illustration. 

| Format | String |
| --- | --- |
|yyyy-MM-dd'T'hh:mm:ss'Z' | *(ISO 8601/default)* 2017-11-28T12:44:22Z |
|yyyy.MM.dd G 'at' HH:mm:ss[ z] | 2017.11.28 AD at 12:44:22 PDT |
|EEE, MMM d, ''yy 'at' h:mm a| Tue, Nov 28, '17 at 12:44 PM |
|YYYY-'W'ww-e K:mm a[, z] | 2017-W48-3 0:44 PM, PDT |
|yyyyy.MMMM.dd GGG hh:mm a | 02017.November.28 AD 12:44 PM |
|EEE, d MMM yyyy HH:mm:ss[ Z] | Tue, 28 Nov 2017 12:44:22 -0700 |
|yyMMddHHmmss[Z] | 171128124422-0700 |
|MM/dd/yyy HH:mm:ss.SSS[Z] | 2017-11-28 12:44:22.123-0700 |
|yyyy-MM-dd'T'HH:mm:ss.SSS[XXX] | 2017-11-28T12:44:22.123-07:00 |
|yyMMdd hh 'o''clock' a[, zzzz] | 171128 12 o'clock PM, Pacific Daylight Time |

`Date` and `Time` use the same formatting rules, with the following rules:
* No field smaller than a day should appear in a `Date` format.  There is no "hour" in the `Date` type: `yyyy-MM-dd`
* No field larger than an hour should appear in a `Time` format.  The is no "day" in the `Time` format: `HH:mm:ss.SSS`

### Number formats

When either the source or destination value is a numeric value **AND** the other is a string, the format is used
in the conversion, as described at [NumberFormat][NumberFormat].  If no format is present, the string is parsed using
the default Java numeric values.

| Format | String |
| --- | --- |
|'#'# | #1, #12345, #-123 |
|$#,##0.00;($#,##0.00) | $1,234.56, $0.50, ($1.00), ($1,234.56) |

Some logical rules apply to the conversions: 
* `Integer` and `Long` formats that include a decimal point will cause an error, for example.

### Examples

| Source type (Avro) | Source value | Format | Destination type | Destination value |   
| --- | --- | --- | --- | --- |
| **int** | `12345` | - | **Long** | :white_check_mark: `12345L` (widening conversion doesn't lose anything) |
| **long** | `12345L` | - | **Integer** | :large_orange_diamond: `12345` (narrowing conversions can be OK, usually on data with few significant digits) |
| **long** | `1234567890123456789L` | - | **Integer** | :large_orange_diamond: `2112454933` (narrowing conversions _can_ lose data, but in a well-defined way.  In this case, the last four bytes of the `long` were reinterpreted as an `int`) |
| **long** | `1234567890123456789L` | - | **Double** | :large_orange_diamond: `1234567890123456770.0d` (some widening conversions can lose precision in a well-defined way!) |
| **long** | `0x8000000000000000L` (MIN_VALUE) | - | **Integer** | `0` (narrowing conversion uses the last four bytes) |
| **string** | `"1234.5"` | - | **Integer** | **Error** -- Can't parse floating point without a format. | 
| **string** | `"1234.5"` | `#` | **Integer** | :hash: `1234` (The format discards after the decimal point) | 
| **string** | `"1234.5"` | `#.#` | **Integer** | :hash: `1234` (Even a format with a decimal point helps convert the input string into a number) | 
| **int** | `1234"` | `#.#` | **String** | :hash: `1234.0` | 
| **boolean** | `false` | - | **Integer** | `0`  | 
| **boolean** | `true` | - | **Integer** | `1`  | 
| **boolean** | `false` | - | **Date** | `1970-01-01` (zero days since 1970-01-01)  | 
| **boolean** | `true` | - | **Date** | `1970-01-02` (one day since 1970-01-01) | 
| **boolean** | `false` | - | **Time** | `00:00:00.000` (zero milliseconds since midnight)  | 
| **boolean** | `true` | - | **Time** | `00:00:00.001` (one milliseconds since midnight, note that if your view doesn't show milliseconds, this will look exactly like `false`!  The underlying data is different, though.) | 
| **timestamp-millis** | `2017-11-28T12:44:22Z` | `yyyyMMdd` | **String** | :clock10: `20171128` | 
| **String** | `20171128` | `yyyyMMdd` | **timestamp-millis** | :clock10: `2017-11-28T00:00:00Z` (hours, minutes and seconds are 0) |
| **String** | `"20171128"` | `yyyyMMdd` | **Date** | :clock10: `2017-11-28` | 
| **int** | `20171128` | - | **Date** | `+57196-09-03` (20,171,128 days after 1970-01-01) | 
| **time-millis** | `12:44:22` | - | **DateTime** | `1970-01-01T12:44:22Z` (Since there's no date part in the source time, 1970-01-01 is used) | 
| **timestamp-millis** | `2017-11-28T12:44:22Z` | - | **Date** | `2017-11-28` (The time component is removed, the underlying number is changed from 1511873062123L to 17498) | 

### Future work
* Refine values to use when source value is null and/or invalid.
* Use AVPath (without predicate) in field?
* Support converting time-zoned strings to date-oriented types.  (The Avro date types do not contain time zone information but an input string can.)
* Decimal type (with precision and scale, in addition to pattern).
* Number values with different grouping and decimal symbols.  Currency?
* Complex types.
* Performance improvements.
* Support suggesting a list of appropriate date/time/number formats depending on the source/destination types.
* Hide the format field when it is not relevant.
* Better error/warning handling -- the type converter is a good candidate to explore what to do when a conversion fails
on one value. 

Window
------

The `Window` component ...
