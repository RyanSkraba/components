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
import org.talend.components.api.properties.IOComponentProperties;
import org.talend.daikon.properties.presentation.Form;

import java.util.Arrays;
import java.util.List;

/**
 * Properties common to input and output Salesforce components.
 */
public class SalesforceConnectionModuleProperties extends IOComponentProperties implements SalesforceProvideConnectionProperties {

    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection"); //$NON-NLS-1$

    public SalesforceModuleProperties module;

    public SalesforceConnectionModuleProperties(String name) {
        super(name);
    }

    @Override
    public boolean supportEmptySchema() {
        return true;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        // Allow for subclassing
        module = new SalesforceModuleProperties("module");
        module.connection = connection;
    }

    public Schema getSchema() {
        return (Schema) module.schema.schema.getValue();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(module.getForm(Form.REFERENCE));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(connection.getForm(Form.ADVANCED));
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
