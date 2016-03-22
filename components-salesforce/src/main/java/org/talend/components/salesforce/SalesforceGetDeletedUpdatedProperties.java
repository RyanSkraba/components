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
package org.talend.components.salesforce;

import org.apache.avro.Schema;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.HasSchemaProperty;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

import java.util.Arrays;
import java.util.List;

import static org.talend.daikon.properties.PropertyFactory.newDate;

public class SalesforceGetDeletedUpdatedProperties extends ComponentProperties implements SalesforceProvideConnectionProperties, HasSchemaProperty {

    public Property startDate = newDate("startDate");

    public Property endDate = newDate("endDate");

    //
    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection");

    public SalesforceModuleProperties module = new SalesforceModuleProperties("module");

    public SalesforceGetDeletedUpdatedProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        module.connection = connection;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(module.getForm(Form.REFERENCE));
        mainForm.addRow(startDate);
        mainForm.addRow(endDate);
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return connection;
    }

    @Override
    public List<Schema> getSchemas() {
        return Arrays.asList(new Schema[]{new Schema.Parser().parse(module.schema.schema.getStringValue())});
    }

    @Override
    public void setSchemas(List<Schema> schemas) {
        module.schema.schema.setValue(schemas.get(0));
    }
}
