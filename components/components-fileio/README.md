components-fileio
=================

The File IO components separate the concepts of: 

* file system (HDFS / S3 / Alluxio / vendor-specific), and
* file/record formats (CSV / Avro / Parquet / JSON).

This offers the following advantages:
 
* adding a new file or record format can be used on all filesystems,
* adding a file system automatically supports all file formats, and
* both are automatically integrated into a single input/output component 
  that automatically according to its configuration. 

simplefileio-definition
-----------------------

The `simplefileio` family of components does *not* implement the design above,
but provides fixed access to the HDFS filesystem and the CSV/Avro/Parquet/JSON 
file formats.
