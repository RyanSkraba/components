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
package org.talend.components.localio.fixed;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * Configures a fixed input component.
 */
public class FixedInputProperties extends FixedConnectorsComponentProperties implements IOProperties<FixedDatasetProperties> {

    public FixedInputProperties(String name) {
        super(name);
    }

    public transient PropertyPathConnector OUT_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "outgoing");

    public SchemaProperties outgoing = new SchemaProperties("outgoing");

    /**
     * The number of times to repeat the input dataset. If the dataset generates random data, specifies the number of rows to
     * generate.
     */
    public Property<Integer> repeat = PropertyFactory.newInteger("repeat", 1);

    /**
     * Hidden property to determine whether the component form permits overriding the dataset values. This can only be set
     * manually in the component.
     */
    public Property<Boolean> useOverrideValues = PropertyFactory.newBoolean("useOverrideValues", false);

    /**
     * If not set to NONE, can be used to override the values in the dataset (either replacing or appending additional records).
     */
    public Property<OverrideValuesAction> overrideValuesAction = PropertyFactory
            .newEnum("overrideValuesAction", OverrideValuesAction.class).setValue(OverrideValuesAction.NONE);

    /** If overriding values is enabled, the data to use for generating components. */
    public Property<String> overrideValues = PropertyFactory.newString("overrideValues", "");

    public transient ReferenceProperties<FixedDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            FixedDatasetDefinition.NAME);

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(repeat);
        mainForm.addRow(overrideValuesAction);
        mainForm.addRow(widget(overrideValues).setWidgetType(Widget.CODE_WIDGET_TYPE)
                .setConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF, "json"));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            // Only add the override values action if the hidden property is set to true.
            form.getWidget(overrideValuesAction).setVisible(useOverrideValues.getValue());
            form.getWidget(overrideValues)
                    .setVisible(useOverrideValues.getValue() && overrideValuesAction.getValue() != OverrideValuesAction.NONE);
        }
    }

    @Override
    public FixedDatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(FixedDatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            // output schema
            connectors.add(OUT_CONNECTOR);
        }
        return connectors;
    }

    public void afterOverrideValuesAction() {
        refreshLayout(getForm(Form.MAIN));
    }

    /**
     * Add or replace the values in the dataset.
     */
    public enum OverrideValuesAction {
        /** Do not override the values in the dataset. */
        NONE,
        /** Replace the values specified in the dataset by the ones specified in this component. */
        REPLACE,
        /** Use the values in this component in addition to the ones specified in the dataset. */
        APPEND
    }
}
