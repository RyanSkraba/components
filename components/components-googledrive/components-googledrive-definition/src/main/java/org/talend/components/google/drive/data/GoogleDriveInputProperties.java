package org.talend.components.google.drive.data;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;

public class GoogleDriveInputProperties extends FixedConnectorsComponentProperties
        implements IOProperties<GoogleDriveDatasetProperties> {

    public ReferenceProperties<GoogleDriveDatasetProperties> dataset = new ReferenceProperties<>("dataset",
            GoogleDriveDatasetDefinition.NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public GoogleDriveInputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public GoogleDriveDatasetProperties getDatasetProperties() {
        return dataset.getReference();
    }

    @Override
    public void setDatasetProperties(GoogleDriveDatasetProperties datasetProperties) {
        dataset.setReference(datasetProperties);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        // unused form, but we need to create one anyway.
        Form mainForm = new Form(this, Form.MAIN);
    }
}
