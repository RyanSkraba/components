package org.talend.components.google.drive.runtime.data;

import java.util.Arrays;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.google.drive.data.GoogleDriveDatasetProperties;
import org.talend.components.google.drive.data.GoogleDriveDatastoreProperties;
import org.talend.components.google.drive.data.GoogleDriveInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class GoogleDriveDatastoreRuntime implements DatastoreRuntime<GoogleDriveDatastoreProperties> {

    protected GoogleDriveDatastoreProperties datastore;

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        GoogleDriveDataSource ds = new GoogleDriveDataSource();
        GoogleDriveInputProperties properties = new GoogleDriveInputProperties("health");
        GoogleDriveDatasetProperties dataset = new GoogleDriveDatasetProperties("data");
        dataset.setDatastoreProperties(datastore);
        properties.setDatasetProperties(dataset);
        ds.initialize(container, properties);

        return Arrays.asList(ds.validate(container));
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, GoogleDriveDatastoreProperties properties) {
        datastore = properties;
        return ValidationResult.OK;
    }
}
