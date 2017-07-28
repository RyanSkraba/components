# tMarketoBulkExec
 
 This component imports a spreadsheet of leads or custom objects into the target instance.

## Parameters

* `bulkImportTo` : import to leads or custom objects.
* `bulkFilePath` : path to file the import file.
* `bulkFileFormat` : import file format. Either csv, tsv or ssv.
* `lookupField`  : (Leads only) field used to dedupe leads. 
* `listId`  : (Leads only)  import leads to this static list.
* `partitionName`  : (Leads only) set leads partition name.
* `customObjectName` : (CustomObject only) Custom object name.
* `pollWaitTime` : time to wait before polling for batch result.
* `logDownloadPath` : to path where is downloaded log files.

The provided file must follow these rules :
* The first row always contains a header that lists the fields to map values of each row into.
* All field names in header must match an API name.
* Remaining rows contain the data to import, one record per row.

**Limitations**

* The size of the file to bulk import is limited to 10MB.

## How the component works

The process is asynchronous, so we have to wait for a given time (`waitPollTime`) before having the importation result.
Once we got the batch result, if needed, the  `failuresLogFile` and `warningsLogFile`  are requested and downloaded to
 the folder provided by the `logDownloadPath` property.

The API returns main informations encapsulated in an `IndexedRecord` with the following informations :

#### Common for both

* `batchId` : the UID of the bulk import. Used to get status, warnings and failures.
* `message` : bulk import message.
* `numOfRowsFailed` : number of failed rows.
* `numOfRowsWithWarning` : number of rows with warnings.
* `status` : _Failed_, _Importing_, _Queued_, _Complete_.
* `failuresLogFile` : path to failures log file provided by the API.
* `warningsLogFile` : path to warnings log file provided by the API.

#### For leads

* `importId` : same as `batchId`.
* `numOfLeadsProcessed` : number of imported records.

#### For custom objects

* `importTime` : time spent on importation.
* `numOfObjectsProcessed` : number of imported records.
* `objectApiName` : the custom object's name for the operation. 
* `operation` : always "import".
