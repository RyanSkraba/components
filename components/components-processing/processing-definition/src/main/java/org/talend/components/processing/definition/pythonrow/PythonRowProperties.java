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
package org.talend.components.processing.definition.pythonrow;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class PythonRowProperties extends FixedConnectorsComponentProperties {

    public PythonRowProperties(String name) {
        super(name);
    }

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public SchemaProperties main = new SchemaProperties("main");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public Property<MapType> mapType = PropertyFactory.newEnum("mapType", MapType.class);

    public Property<String> pythonCode = PropertyFactory.newString("pythonCode");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(main.getForm(Form.REFERENCE));
        mainForm.addRow(mapType);
        mainForm.addRow(widget(pythonCode).setWidgetType(Widget.CODE_WIDGET_TYPE)
                .setConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF, "python"));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        mapType.setValue(MapType.FLATMAP);
        afterMapType();
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    public void afterChangeSchema() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterMapType() {
        if (MapType.MAP.equals(mapType.getValue())) {
            StringBuilder sb = new StringBuilder();
            sb.append("# Here you can define your custom MAP transformations on the input\n");
            sb.append("# The input record is available as the \"input\" variable\n");
            sb.append("# The output record is available as the \"output\" variable\n");
            sb.append("# The record columns are available as defined in your input/output schema\n");
            sb.append("# The return statement is added automatically to the generated code,\n");
            sb.append("# so there's no need to add it here\n\n");
            sb.append("# Code Sample :\n\n");
            sb.append("# output['col1'] = input['col1'] + 1234\n");
            sb.append("# output['col2'] = \"The \" + input['col2'] + \":\"\n");
            sb.append("# output['col3'] = CustomTransformation(input['col3'])\n");
            pythonCode.setValue(sb.toString());
        } else { // FlapMap
            StringBuilder sb = new StringBuilder();
            sb.append("# Here you can define your custom FLATMAP transformations on the input\n");
            sb.append("# The input record is available as the \"input\" variable\n");
            sb.append("# The columns are available as defined in your input/output schema\n");
            sb.append("# The output list is available as the variable \"outputList\"\n");
            sb.append("# The return statement is added automatically to the generated code,\n");
            sb.append("# so there's no need to add it here\n\n");
            sb.append("# Code Sample:\n\n");
            sb.append("# output = json.loads(\"{}\")\n");
            sb.append("# output['col1'] = CustomTransformation1(input['col1'])\n");
            sb.append("# outputList.append(output)\n\n");
            sb.append("# output2 = json.loads(\"{}\")\n");
            sb.append("# output2['col1'] = CustomTransformation2(input['col1'])\n");
            sb.append("# outputList.append(output2)\n");
            pythonCode.setValue(sb.toString());
        }
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

}
