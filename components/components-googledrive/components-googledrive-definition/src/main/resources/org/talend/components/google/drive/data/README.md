# Notes on GoogleDrive Datastore/Dataset support for DataStream, DataPrep, etc. products

## Datastore OAuth2 Connection mode
 
Actually, only [Service Account](https://developers.google.com/identity/protocols/OAuth2ServiceAccount) is supported 
with a JSON file.

You can create a JSON service account file [here](https://console.developers.google.com/permissions/serviceaccounts).

## Service Account File Location

The location of the service account credentials is specified by a `System Property` which key is `org.talend.components.google.drive.service_account_file`.

See [System Properties](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html) for setting 
properties.



