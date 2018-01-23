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
package org.talend.components.marketo;

import static org.talend.daikon.properties.property.PropertyFactory.newEnum;

import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public abstract class MarketoComponentProperties extends FixedConnectorsComponentProperties
        implements MarketoProvideConnectionProperties {

    public TMarketoConnectionProperties connection = new TMarketoConnectionProperties("connection");

    /**/
    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaInput");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public ISchemaListener schemaListener;

    public SchemaProperties schemaInput = new SchemaProperties("schemaInput") {

        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }
    };

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject");

    private static final long serialVersionUID = 5587867978797981L;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoComponentProperties.class);

    public MarketoComponentProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(schemaInput.getForm(Form.REFERENCE));
        // Advanced
        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(connection.getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    public List<String> getSchemaFields() {
        return MarketoUtils.getSchemaFields(schemaInput.schema.getValue());
    }

    @Override
    public TMarketoConnectionProperties getConnectionProperties() {
        return connection.getConnectionProperties();
    }

    public Boolean isApiSOAP() {
        return APIMode.SOAP.equals(getConnectionProperties().apiMode.getValue());
    }

    public Boolean isApiREST() {
        return APIMode.REST.equals(getConnectionProperties().apiMode.getValue());
    }

    protected <T extends Enum<T>> Property<T> checkForInvalidStoredEnumProperty(Property<T> property, Class<T> fixEnum) {
        String name = property.getName();
        Object o;
        String value = null;
        LinkedHashMap ov;
        if (property.getStoredValue() instanceof Enum && fixEnum.getCanonicalName().equals(property.getType())) {
            return property;
        }
        o = property.getStoredValue();
        if (o instanceof LinkedHashMap) {
            ov = (LinkedHashMap) o;
            value = String.valueOf(ov.get("name"));
        }
        if (o instanceof String || o instanceof Enum) {
            value = String.valueOf(o);
        }
        if (value == null) {
            LOG.warn("[checkForInvalidStoredEnumProperty] Cannot determine value for enum {} stored value: {} ({}).", name, o,
                    o.getClass().getCanonicalName());
            // don't break everything for that...
            return property;
        }
        try {
            LOG.warn("[checkForInvalidStoredEnumProperty] Fixing enum {} value: {}", name, value);
            property = newEnum(name, fixEnum);
            property.setValue(Enum.valueOf(fixEnum, value));
            property.setStoredValue(Enum.valueOf(fixEnum, value));
            property.setPossibleValues(fixEnum.getEnumConstants());
        } catch (Exception e) {
            LOG.error("[checkForInvalidStoredEnumProperty] Error during {} fix: {}.", name, e);
        }
        return property;
    }

}
