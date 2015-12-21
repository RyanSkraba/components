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
package org.talend.components.mongodb.tmongodbinput;

import static org.talend.components.api.properties.PropertyFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

/**
 * NOT DEVELOPPED
 */
public class MongoDBInputColumnProperties extends ComponentProperties {

    public Property uselessBoolean = newBoolean("uselessBoolean"); //$NON-NLS-1$

    //
    // Advanced
    //
    public Property uselessInteger = newInteger("uselessInteger"); //$NON-NLS-1$

    public MongoDBInputColumnProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        returns = setReturnsProperty();
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_LINE");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(uselessBoolean);
        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(uselessInteger);
    }

    public void afterQueryMode() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterManualQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

}
