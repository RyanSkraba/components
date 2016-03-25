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

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.HasSchemaProperty;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceProvideConnectionProperties;
import org.talend.daikon.properties.presentation.Form;

import java.util.Arrays;
import java.util.List;

public class TSalesforceGetServerTimestampProperties extends ComponentProperties
        implements SalesforceProvideConnectionProperties, HasSchemaProperty {

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
        // FIXME - need to specify the date type
        Schema s = SchemaBuilder.record("Root").fields().name("ServerTimeStamp").type().stringType().noDefault().endRecord();
        schema.schema.setValue(s);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(schema.getForm(Form.REFERENCE));
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return connection;
    }

    @Override
    public List<Schema> getSchemas() {
        return Arrays.asList(new Schema[]{new Schema.Parser().parse(schema.schema.getStringValue())});
    }

    @Override
    public void setSchemas(List<Schema> schemas) {
        schema.schema.setValue(schemas.get(0));
    }

}
