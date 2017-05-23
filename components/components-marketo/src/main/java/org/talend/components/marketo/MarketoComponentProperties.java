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

import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.DEFAULT_ENDPOINT_REST;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.DEFAULT_ENDPOINT_SOAP;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.migration.SerializeSetVersion;

public abstract class MarketoComponentProperties extends FixedConnectorsComponentProperties
        implements MarketoProvideConnectionProperties, SerializeSetVersion {

    public Property<APIMode> apiMode = newEnum("apiMode", APIMode.class);

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

    public Property<Integer> batchSize = newInteger("batchSize");

    public Property<Boolean> dieOnError = newBoolean("dieOnError");

    private static final long serialVersionUID = 5587867978797981L;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoComponentProperties.class);

    public MarketoComponentProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        apiMode.setValue(MarketoComponentProperties.APIMode.REST);
        connection.setApiMode(apiMode.getValue());

        batchSize.setValue(100);
        dieOnError.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addColumn(apiMode);
        mainForm.addRow(schemaInput.getForm(Form.REFERENCE));
        // Advanced
        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(connection.getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (DEFAULT_ENDPOINT_REST.equals(connection.endpoint.getValue())
                && MarketoComponentProperties.APIMode.SOAP.equals(apiMode.getValue())) {
            connection.endpoint.setValue(DEFAULT_ENDPOINT_SOAP);
        }
        if (DEFAULT_ENDPOINT_SOAP.equals(connection.endpoint.getValue())
                && MarketoComponentProperties.APIMode.REST.equals(apiMode.getValue())) {
            connection.endpoint.setValue(DEFAULT_ENDPOINT_REST);
        }

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    public List<String> getSchemaFields() {
        return getSchemaFields(schemaInput.schema.getValue());
    }

    public List<String> getSchemaFields(Schema schema) {
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : schema.getFields()) {
            fieldNames.add(f.name());
        }
        return fieldNames;
    }

    public Schema newSchema(Schema metadataSchema, String newSchemaName, List<Schema.Field> moreFields) {
        Schema newSchema = Schema.createRecord(newSchemaName, metadataSchema.getDoc(), metadataSchema.getNamespace(),
                metadataSchema.isError());

        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : metadataSchema.getFields()) {
            Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
            field.getObjectProps().putAll(se.getObjectProps());
            for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }
        copyFieldList.addAll(moreFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String, Object> entry : metadataSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

    @Override
    public TMarketoConnectionProperties getConnectionProperties() {
        TMarketoConnectionProperties conn = connection.getConnectionProperties();
        // ensure that connection and component use the same APIMode
        conn.setApiMode(getApiMode());
        return conn;
    }

    public Boolean isApiSOAP() {
        return APIMode.SOAP.equals(apiMode.getValue());
    }

    public Boolean isApiREST() {
        return APIMode.REST.equals(apiMode.getValue());
    }

    public enum APIMode {
        REST,
        SOAP
    }

    @Override
    public APIMode getApiMode() {
        return apiMode.getValue();
    }

    public void afterApiMode() {
        getConnectionProperties().setApiMode(getApiMode());
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean migrated = super.postDeserialize(version, setup, persistent);
        if (version < this.getVersionNumber()) {
            if (apiMode.getValue() == null) {
                LOG.info("[postDeserialize] Migrating API to {}.", connection.apiMode.getValue());
                apiMode.setValue(APIMode.valueOf(connection.apiMode.getValue().name()));
                migrated = true;
            }
        }
        return migrated;
    }

}
