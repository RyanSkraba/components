// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.tmarklogicbulkload;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

public class MarkLogicBulkLoadProperties extends ComponentPropertiesImpl {

    public MarkLogicBulkLoadProperties(String name) {
        super(name);
    }

    public MarkLogicConnectionProperties connection = new MarkLogicConnectionProperties("connection");

    public Property<String> loadFolder = newString("loadFolder");

    public Property<String> docidPrefix = newString("docidPrefix");

    public Property<String> mlcpParams = newString("mlcpParams");

    public Property<Boolean> useExternalMLCP = newBoolean("useExternalMLCP");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        connection.getForm(Form.MAIN).getWidget(connection.authentication).setHidden();
        mainForm.addRow(Widget.widget(loadFolder).setWidgetType(Widget.DIRECTORY_WIDGET_TYPE));
        mainForm.addRow(docidPrefix);

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(mlcpParams);
        advancedForm.addRow(useExternalMLCP);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        connection.setupProperties();

        loadFolder.setRequired();
        useExternalMLCP.setValue(false); // use internal mlcp library by default
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {
            for (Form childForm : connection.getForms()) {
                connection.refreshLayout(childForm);
            }
            connection.getForm(Form.MAIN).getWidget(connection.authentication).setHidden();
        }
    }

    public MarkLogicConnectionProperties getConnection() {
        return connection.isReferencedConnectionUsed() ? connection.referencedComponent.getReference() : connection;
    }
}
