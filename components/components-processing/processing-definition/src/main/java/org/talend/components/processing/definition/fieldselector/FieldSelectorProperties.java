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
package org.talend.components.processing.definition.fieldselector;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.daikon.properties.PropertiesList;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class FieldSelectorProperties extends FixedConnectorsComponentProperties implements Serializable {

    public static final int MAX_SELECTORS = 100;

    public PropertiesList<SelectorProperties> selectors =
            new PropertiesList<>("selectors", new PropertiesList.NestedPropertiesFactory<SelectorProperties>() {

                @Override
                public SelectorProperties createAndInit(String name) {
                    return (SelectorProperties) new SelectorProperties(name).init();
                }
            });

    public transient PropertyPathConnector INCOMING_CONNECTOR =
            new PropertyPathConnector(Connector.MAIN_NAME, "incoming");

    public transient PropertyPathConnector OUTGOING_CONNECTOR =
            new PropertyPathConnector(Connector.MAIN_NAME, "outgoing");

    public FieldSelectorProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            connectors.add(OUTGOING_CONNECTOR);
        } else {
            // input schema
            connectors.add(INCOMING_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        selectors.setMaxItems(String.valueOf(MAX_SELECTORS));
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(selectors).setWidgetType(Widget.NESTED_PROPERTIES).setConfigurationValue(
                Widget.NESTED_PROPERTIES_TYPE_OPTION, "selector"));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        setupLayout();
        // Add a default converter
        selectors.init();
        selectors.createAndAddRow();
    }
}
