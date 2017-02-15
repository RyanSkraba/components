components-localio
==================

The Local IO components act as sources and sinks for data, but without any specific underlying "support" like a database or file.  

This is useful in a testing environment, for generating data sets, or even in production jobs for small tables of fixed data.
 

FixedFlowInput
--------------

The `FixedFlowInput` component provides a source of constant, unchanged data.


RowGenerator
------------

The `RowGenerator` component can be used to create a dataset of generated data.

The schema is set in the component, and rows are generated according to annotations in the schema.

Annotations are described below, but if no annotations are present, the following logic is used:

* Primitives:
  - `ENUM`: Pick one of the possible values randomly.
  - `STRING`: Create a random 10 character alphanumeric string.
  - `BYTES`: Create a random byte array of 0-10 bytes. 
  - `FIXED`: Create a random byte array of the specified size.
  - `INT`: Pick a random integer.
  - `LONG`: Pick a random long.
  - `FLOAT`: Pick a random float between 0 and 1.
  - `DOUBLE`: Pick a random double between 0 and 1.
  - `BOOLEAN`: Pick true or false randomly.
  - `NULL`: Always null.
* Composite: (the annotation on the inner Schemas are used to create the values.)  
  - `RECORD`: Create a random value for each of the fields.
  - `ARRAY`: Create an array of 0-10 items.
  - `MAP`: Create an map of 0-10 values, using a 10 character alphanumeric string as the key.
  - `UNION`: Pick one of the possible values with equal weight.  Note that if a Schema is nullable (i.e. `UNION` with `NULL`), the value will be `null` 50% of the time.
 
### Seed

If a seed is set in the `RowGenerator`, the generated rows are guaranteed to be the same for subsequent runs, as long as the same schema and generator annotations are used.

TODO(rskraba): The following discussion on bundle shaping and advanced row generation options is not fully implemented.

### Shaping

The following properties control how the data is generated.

* Number of partitions
* Number of rows per partition

### Numeric annotations

The following annotations can be added to the schema for `INT`, `LONG`, `FLOAT` and `DOUBLE` types:

| name | value | 
|------|-------|
| guassian |          |      
| exponential |          |      
| min  |      |      
| max  |      |
| sequence |      |

### Common annotations

The following annotation can be added to the schema for `MAP`, `ARRAY`, `STRING` and `BYTES` types to control the size of the output, or the number of  elements.

| name | value | 
|------|-------|
| length |          |      

### UNION annotations

The following annotation can be added to the schemas in a `UNION` to control how often they are picked.

| name | value | 
|------|-------|
| weight |          |      

### ENUM annotations

The following annotation can be added to the schemas in an `ENUM` to control how often they are picked.

| name | value | 
|------|-------|
| weights | |      

### Custom annotations

| name | value | 
|------|-------|
| custom |          |      
