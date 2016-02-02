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

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaFactory;
import org.talend.daikon.schema.SchemaElement.Type;

public class TSalesforceGetServerTimestampProperties extends ComponentProperties {

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
        Schema s = (Schema) schema.schema.getValue();
        s.setRoot(SchemaFactory.newSchemaElement(Type.GROUP, "Root"));
        s.getRoot().addChild(PropertyFactory.newDate("ServerTimestamp"));
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(schema.getForm(Form.REFERENCE));
    }

}
