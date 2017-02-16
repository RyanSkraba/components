// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.processing.window;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class WindowProperties extends FixedConnectorsComponentProperties implements Serializable {

    public Property<Integer> windowLength = PropertyFactory.newInteger("windowLength").setRequired().setValue(-1);

    public Property<Integer> windowSlideLength = PropertyFactory.newInteger("windowSlideLength").setValue(-1);

    public Property<Boolean> windowSession = PropertyFactory.newBoolean("windowSession").setValue(false);

    // main
    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public WindowProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            // output schema
            connectors.add(FLOW_CONNECTOR);
        } else {
            // input schema
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(windowLength);
        mainForm.addRow(windowSlideLength);
        mainForm.addRow(windowSession);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        windowLength.setValue(-1);
        windowSlideLength.setValue(-1);
        windowSession.setValue(false);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(windowSlideLength.getName()).setHidden(windowSession);
        }
    }

    public void afterWindowSession() {
        refreshLayout(getForm(Form.MAIN));
    }
}
