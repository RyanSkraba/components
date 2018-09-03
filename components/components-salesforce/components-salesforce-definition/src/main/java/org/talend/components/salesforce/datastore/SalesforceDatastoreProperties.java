// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.datastore;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class SalesforceDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public static final String CONFIG_FILE_lOCATION_KEY = "org.talend.component.salesforce.config.file";

    /** Properties file key for endpoint storage. */
    public static final String ENDPOINT_PROPERTY_KEY = "endpoint";

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceDatastoreProperties.class);

    public Property<String> userId = newProperty("userId").setRequired();

    public Property<String> password = newProperty("password").setRequired().setFlags(
            EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> securityKey = newProperty("securityKey").setRequired().setFlags(
            EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> endpoint = newString("endpoint").setRequired();

    private Properties salesforceProperties = null;

    public SalesforceDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {

        super.setupProperties();
        loadEndPoint();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = Form.create(this, Form.MAIN);

        mainForm.addRow(endpoint);
        mainForm.addRow(userId);
        mainForm.addRow(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(widget(securityKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

    /**
     * Load Dataprep user Salesforce properties file for endpoint & timeout default values.
     *
     * @return a {@link Properties} objet (maybe empty) but never null.
     */
    public Properties getSalesforceProperties() {
        if (salesforceProperties == null) {
            salesforceProperties = new Properties();
            String config_file = System.getProperty(CONFIG_FILE_lOCATION_KEY);
            try (InputStream is = config_file != null ? (new FileInputStream(config_file)) : null) {
                if (is == null) {
                    LOG.warn("not found the property file, will use the default value for endpoint and timeout");
                } else {
                    salesforceProperties.load(is);
                }
            } catch (IOException e) {
                LOG.warn("not found the property file, will use the default value for endpoint and timeout", e);
            }
        }
        return salesforceProperties;
    }

    /**
     * Load the Salesforce endpoint, if necessary either from the user specified property file or from the default
     * hardcoded value.
     */
    private void loadEndPoint() {
        if (endpoint == null || endpoint.getValue() == null) {
            salesforceProperties = getSalesforceProperties();
            String endpointProp = salesforceProperties.getProperty(ENDPOINT_PROPERTY_KEY);
            if (endpointProp != null && !endpointProp.isEmpty()) {
                if (endpointProp.contains(SalesforceConnectionProperties.RETIRED_ENDPOINT)) {
                    // FIXME : is this code still up to date ? if so, have we documented it for our customers ?
                    endpointProp = endpointProp.replaceFirst(SalesforceConnectionProperties.RETIRED_ENDPOINT,
                            SalesforceConnectionProperties.ACTIVE_ENDPOINT);
                }
                endpoint.setValue(endpointProp);
            } else {
                endpoint.setValue(SalesforceConnectionProperties.URL);
            }
        }
    }

    /**
     * Return the datastore endpoint, loading a default value if no value is present.
     * 
     * @return the datastore endpoint value.
     */
    public String getEndPoint() {
        loadEndPoint();
        return endpoint.getValue();
    }
}
