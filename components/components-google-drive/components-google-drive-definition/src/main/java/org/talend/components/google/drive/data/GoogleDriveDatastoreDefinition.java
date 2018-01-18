package org.talend.components.google.drive.data;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDriveDatastoreDefinition extends I18nDefinition
        implements DatastoreDefinition<GoogleDriveDatastoreProperties> {

    public static final String NAME = "GoogleDriveDatastore";

    public GoogleDriveDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public DatasetProperties createDatasetProperties(GoogleDriveDatastoreProperties storeProp) {
        GoogleDriveDatasetProperties ds = new GoogleDriveDatasetProperties("data");
        ds.init();
        ds.setDatastoreProperties(storeProp);

        return ds;
    }

    @Override
    public String getInputCompDefinitionName() {
        return GoogleDriveInputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        return null;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(GoogleDriveDatastoreProperties properties) {
        return GoogleDriveComponentDefinition.getCommonRuntimeInfo(GoogleDriveComponentDefinition.DATASTORE_RUNTIME_CLASS);
    }

    @Override
    public Class<GoogleDriveDatastoreProperties> getPropertiesClass() {
        return GoogleDriveDatastoreProperties.class;
    }

    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public String getImagePath(DefinitionImageType definitionImageType) {
        switch (definitionImageType) {
        case PALETTE_ICON_32X32:
            return NAME + "_icon32.png";
        default:
           return null;
        }
    }

    @Override
    public String getIconKey() {
        return null;
    }
}
