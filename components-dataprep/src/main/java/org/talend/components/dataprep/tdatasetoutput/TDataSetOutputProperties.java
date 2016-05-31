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
package org.talend.components.dataprep.tdatasetoutput;

import java.util.Collections;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.dataprep.runtime.DataPrepOutputModes;
import org.talend.components.dataprep.runtime.RuntimeProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * The ComponentProperties subclass provided by a component stores the configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is provided at design-time to configure a
 * component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the properties to the user.</li>
 * </ol>
 * 
 * The TDataSetOutputProperties has two properties:
 * <ol>
 * <li>{code dataSetName}, a simple property which is a String containing the file path that this component will read.
 * </li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class TDataSetOutputProperties extends FixedConnectorsComponentProperties {

    public Property<String> login = PropertyFactory.newString("login");

    public Property<String> pass = PropertyFactory.newString("pass");

    public Property<String> url = PropertyFactory.newString("url");

    public Property<DataPrepOutputModes> mode = PropertyFactory.newEnum("mode", DataPrepOutputModes.class);

    public Property<String> dataSetName = PropertyFactory.newString("dataSetName");

    public Property<Integer> limit = PropertyFactory.newInteger("limit", 100);

    public SchemaProperties schema = new SchemaProperties("schema");

    protected transient PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    public TDataSetOutputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mainConnector);
        }
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = new Form(this, Form.MAIN);
        form.addRow(schema.getForm(Form.REFERENCE));
        form.addRow(url);
        form.addRow(login);
        form.addRow(Widget.widget(pass).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        form.addRow(mode);
        form.addRow(dataSetName);
        form.addRow(limit);
    }

    public Schema getSchema() {
        return schema.schema.getValue();
    }

    public RuntimeProperties getRuntimeProperties() {
        RuntimeProperties runtimeProperties = new RuntimeProperties();
        runtimeProperties.setUrl(url.getStringValue());
        runtimeProperties.setLogin(login.getStringValue());
        runtimeProperties.setPass(pass.getStringValue());
        runtimeProperties.setDataSetName(dataSetName.getStringValue());
        runtimeProperties.setMode(mode.getValue());
        runtimeProperties.setLimit(limit.getStringValue());
        return runtimeProperties;
    }
}
