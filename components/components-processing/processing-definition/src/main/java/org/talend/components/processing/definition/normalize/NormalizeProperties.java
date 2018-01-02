// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.processing.definition.normalize;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class NormalizeProperties extends FixedConnectorsComponentProperties {

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void afterSchema() {
            updateOutputSchemas();
        }
    };

    public Property<String> columnToNormalize = PropertyFactory.newString("columnToNormalize", "").setRequired();

    public Property<Boolean> isList = PropertyFactory.newBoolean("isList", false);

    public Property<String> fieldSeparator = PropertyFactory.newString("fieldSeparator", NormalizeConstant.Delimiter.SEMICOLON)
            .setPossibleValues(NormalizeConstant.FIELD_SEPARATORS);

    public Property<String> otherSeparator = PropertyFactory.newString("otherSeparator", "");

    public Property<Boolean> discardTrailingEmptyStr = PropertyFactory.newBoolean("discardTrailingEmptyStr", false);

    public Property<Boolean> trim = PropertyFactory.newBoolean("trim", false);

    public transient ISchemaListener schemaListener;

    public NormalizeProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(columnToNormalize).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        mainForm.addRow(isList);
        mainForm.addRow(fieldSeparator);
        mainForm.addRow(otherSeparator);
        mainForm.addRow(discardTrailingEmptyStr);
        mainForm.addRow(trim);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        columnToNormalize.setValue("");
        isList.setValue(false);
        fieldSeparator.setValue(NormalizeConstant.Delimiter.SEMICOLON);
        otherSeparator.setValue("");
        discardTrailingEmptyStr.setValue(false);
        trim.setValue(false);
        schemaListener = new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateOutputSchemas();
            }

        };
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(fieldSeparator.getName()).setHidden(isList);
            form.getWidget(otherSeparator.getName()).setHidden(isList);
            if (form.getWidget(otherSeparator.getName()).isVisible()) {
                form.getWidget(otherSeparator.getName())
                        .setVisible(fieldSeparator.getValue().equals(NormalizeConstant.Delimiter.OTHER));
            }
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new LinkedHashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            connectors.add(FLOW_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    protected void updateOutputSchemas() {
        Schema inputSchema = main.schema.getValue();
        schemaFlow.schema.setValue(inputSchema);
    }

    public void afterIsList() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterFieldSeparator() {
        refreshLayout(getForm(Form.MAIN));
    }
}
