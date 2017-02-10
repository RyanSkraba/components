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
package org.talend.components.azurestorage.queue.tazurestoragequeueoutput;

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;

public class TAzureStorageQueueOutputProperties extends AzureStorageQueueProperties {

    private static final long serialVersionUID = 3427627996958495784L;

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public ISchemaListener schemaListener;

    public TAzureStorageQueueOutputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        Schema s = SchemaBuilder.builder().record("Main").fields()//
                // .name(FIELD_MESSAGE_ID).prop(SchemaConstants.TALEND_COLUMN_IS_KEY,
                // "true").type(AvroUtils._string()).noDefault()//
                .name(FIELD_MESSAGE_CONTENT).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault() //
                .endRecord();
        schema.schema.setValue(s);
        schemaListener = new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateOutputSchemas();
            }
        };
    }

    @Override
    public void setupLayout() {
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(queueName);
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        mainForm.addRow(dieOnError);

        Form advanced = new Form(this, Form.ADVANCED);
        advanced.addRow(timeToLiveInSeconds);
        advanced.addRow(initialVisibilityDelayInSeconds);
    }

    @Override
    public Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(FLOW_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        updateOutputSchemas();
    }

    protected void updateOutputSchemas() {
        Schema inputSchema = schema.schema.getValue();
        schemaFlow.schema.setValue(inputSchema);
    }
}
