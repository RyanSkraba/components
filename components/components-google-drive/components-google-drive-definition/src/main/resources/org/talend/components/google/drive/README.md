# GoogleDrive Components

## Rules for specifying a resource location

There are three access methods for specifying a resource identification / location.

#### Global search

When specifying a `folerName` or `fileName` alone, a global search will be done in the Google Drive.

Examples:
* `Work`
* `2017-Q3.xlsx`
* `README.md`

If there's more than one resource matching, an error will be thrown because the resource cannot be located precisely.   

#### _Pseudo path hierarchy_

You can specify a path like in a filesystem. It's assuming that the path starts by the GoogleDrive `root` folder.
You can have a nested path to the resource. 

The use of a **`/`** or more in the resource name triggers this access method.

Examples:
* `/Work/DI/TalendJobs/Reporting`
* `/Work/DI/TalendJobs/Reporting/2017-Q3.xlxs`
* `/Untitled Drawing`

![](readme/GoogleDriveNestedFolder.png "/Work/DI/TalendJobs/Reporting/")

When given a `folderName` the value `/Work/DI/TalendJobs/Reporting`, the target folder `Reporting` will be in the 
parents of `TalendJobs` which will be in the parents of `DI`  which will be in the parents of `Work` which will be in the 
 parents of the drive's `root`.
 
```
root
└───Work
    └───DI
        └───TalendJobs
            └───Reporting
```

![](readme/GoogleDriveNestedFile.png "/Work/DI/TalendJobs/Reporting/2017-Q3.xlsx")

When given a `fileName` the value `/Work/DI/TalendJobs/Reporting/2017-Q3.xlsx`, the target file `2017-Q3.xlsx`  will be 
in the parents of `Reporting` will be in the parent which will be in the parent of `TalendJobs` which will be in the 
parents of `DI`  which will be in the parents of `Work` which will be in the parents of the drive's `root`. 
                                                                                                                                          of `TalendJobs` which will be in the parents of `DI`  which will be in the parents of `Work` which will be in the 
```
root
└───Work
    └───DI
        └───TalendJobs
            └───Reporting
                └───2017-Q3.xlsx
```


#### Resource `id` 

You can specify the `id` of the targeted resource. With this method, there's no ambiguity to locate the target.
This is the most efficient way to access to a resource.

Examples:
* `root`
* `1QmPtBteAjxxnhEKNDJEzF61C453DuDia3FMyCMItyww`
* `1r08uj6puhiAm17N-hh7lsyBYnP0ZgLka_jhP1-kgjuk`
* `1Ky8fU1npVEweb20t_NYombRS0DiIngmDC4jKsrNI9B4`

Implemented in
* [X] `tGoogleDriveList`
* [X] `tGoogleDriveCreate` 
* [X] `tGoogleDriveDelete`
* [X] `tGoogleDriveCopy` 
* [X] `tGoogleDriveGet`
* [X] `tGoogleDrivePut`
