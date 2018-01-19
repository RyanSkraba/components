package org.talend.components.google.drive.data;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class GoogleDriveDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public Property<String> serviceAccountJSONFile = newProperty("serviceAccountJSONFile").setRequired();

    public static final String SERVICE_ACCOUNT_FILE = "org.talend.components.google.drive.service_account_file";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveDatastoreProperties.class);

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveDatastoreProperties.class);

    public GoogleDriveDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        String saPath = System.getProperty(SERVICE_ACCOUNT_FILE);
        if (StringUtils.isEmpty(saPath)) {
            LOG.warn((messages.getMessage("error.env.not.set")));
            saPath = this.getClass().getClassLoader().getResource("service_account.json").getFile();
        }
        serviceAccountJSONFile.setValue(saPath);
    }

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(serviceAccountJSONFile);
    }
}
