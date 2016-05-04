// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.tsalesforcegetservertimestamp;

import java.util.Collections;
import java.util.Set;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceProvideConnectionProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.util.AvroTypes;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.talend6.Talend6SchemaConstants;

public class TSalesforceGetServerTimestampProperties extends FixedConnectorsComponentProperties
        implements SalesforceProvideConnectionProperties {

    //
    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection");

    // Just holds the server timestamp
    public SchemaProperties schema = new SchemaProperties("schema");

    public TSalesforceGetServerTimestampProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        Schema s = SchemaBuilder.record("Main")
                .fields().name("ServerTimeStamp")
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "20")//$NON-NLS-1$
                .type(AvroTypes._date()).noDefault().endRecord();
        schema.schema.setValue(s);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(schema.getForm(Form.REFERENCE));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(connection.getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.ADVANCED)) {
            form.getChildForm(connection.getName()).getWidget(connection.bulkConnection.getName()).setHidden(true);
            form.getChildForm(connection.getName()).getWidget(connection.httpTraceMessage.getName()).setHidden(true);
        }
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return connection;
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "schema"));
        } else {
            return Collections.EMPTY_SET;
        }
    }

}
