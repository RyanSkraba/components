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
package org.talend.components.localio.fixedflowinput;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class FixedFlowInputProperties extends FixedConnectorsComponentProperties {

    public FixedFlowInputProperties(String name) {
        super(name);
    }

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public Property<Integer> nbRows = PropertyFactory.newInteger("nbRows", 1);

    public Property<String> values = PropertyFactory.newString("values", "");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(schemaFlow.getForm(Form.REFERENCE));
        mainForm.addRow(nbRows);
        mainForm.addRow(values);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        nbRows.setValue(1);
        values.setValue("");
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // everything is always visible
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            // output schema
            connectors.add(FLOW_CONNECTOR);
        }
        return connectors;
    }

}
