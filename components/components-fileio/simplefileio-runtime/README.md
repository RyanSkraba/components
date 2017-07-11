simplefileio-runtime tests on windows
=====================================

S3 component requires additional environment setup to work on Windows OS:
* create `%HADOOP_HOME%\bin` directory. You may use any path as %HADOOP_HOME% 
* Put *hadoop.dll* into `%HADOOP_HOME%\bin`
* Put *winutils.exe* into `%HADOOP_HOME%\bin`
* Set **HADOOP_HOME** environmental variable
* Append `%HADOOP_HOME%\bin;` to **PATH** environmental variable  


You may find required binaries in 

https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-winutils

or

https://github.com/steveloughran/winutils