package org.talend.components.google.drive.runtime.data;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.data.GoogleDriveDatasetProperties;
import org.talend.components.google.drive.data.GoogleDriveDatastoreProperties;
import org.talend.components.google.drive.data.GoogleDriveInputProperties;
import org.talend.components.google.drive.runtime.GoogleDriveUtils;
import org.talend.components.google.drive.runtime.client.GoogleDriveCredentialWithServiceAccount;
import org.talend.components.google.drive.runtime.client.GoogleDriveService;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.User;

public class GoogleDriveDataSource implements BoundedSource {

    public static final String SCHEMA_NAME = "GoogleDriveList";

    public static final String APPLICATION_NAME = "DATASTREAM (GPN:Talend)";

    private RuntimeContainer container;

    private GoogleDriveInputProperties properties;

    private GoogleDriveDatasetProperties dataset;

    private GoogleDriveDatastoreProperties datastore;

    private String serviceAccountFile;

    private Drive service;

    private GoogleDriveUtils utils;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveDataSource.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.container = container;
        this.properties = (GoogleDriveInputProperties) properties;
        dataset = this.properties.getDatasetProperties();
        datastore = this.dataset.getDatastoreProperties();
        serviceAccountFile = datastore.serviceAccountJSONFile.getValue();

        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult vr = new ValidationResultMutable(Result.OK);
        // check for Connection attributes
        if (StringUtils.isEmpty(serviceAccountFile)) {
            vr = new ValidationResultMutable(Result.ERROR).setMessage("Service Account JSON File cannot be empty.");
            return vr;
        }
        try {
            // make a dummy call to check drive's connection..
            User u = getDriveService().about().get().setFields("user").execute().getUser();
            LOG.debug("[validate] Testing User Properties: {}.", u);
        } catch (Exception ex) {
            LOG.error("[validate] {}.", ex.getMessage());
            vr = new ValidationResultMutable(Result.ERROR).setMessage(ex.getMessage());
            return vr;
        }

        return ValidationResult.OK;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        return new GoogleDriveInputReader(adaptor, this, properties);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> schemas = new ArrayList<>();
        schemas.add(new SimpleNamedThing(SCHEMA_NAME, SCHEMA_NAME));
        return schemas;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return properties.getDatasetProperties().getSchema();
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    public Credential getCredential() throws IOException, GeneralSecurityException {
        return GoogleDriveCredentialWithServiceAccount.builder().serviceAccountJSONFile(new File(serviceAccountFile)).build();
    }

    public Drive getDriveService() throws GeneralSecurityException, IOException {
        if (service == null) {
            service = new GoogleDriveService(APPLICATION_NAME, newTrustedTransport(), getCredential()).getDriveService();
        }
        return service;
    }

    public GoogleDriveUtils getDriveUtils() throws GeneralSecurityException, IOException {
        if (utils == null) {
            utils = new GoogleDriveUtils(getDriveService());
        }
        return utils;
    }
}
