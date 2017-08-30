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
package org.talend.components.processing.definition.filterrow;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.PropertiesList;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * Contains a nested list of {@link FilterRowCriteriaProperties} so that we can combine several filtering criterias
 */

public class FilterRowProperties extends FixedConnectorsComponentProperties {

    // Define the Property

    // input schema
    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void afterSchema() {
            updateOutputSchemas();
        }
    };

    // output schema
    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    // reject schema
    public transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject");

    // how to combine filters
    public Property<LogicalOpType> logicalOp = PropertyFactory.newEnum("logicalOp",
            LogicalOpType.class);

    // list of filters
    public PropertiesList<FilterRowCriteriaProperties> filters = new PropertiesList<>("filters",
            new PropertiesList.NestedPropertiesFactory<FilterRowCriteriaProperties>() {

                @Override
                public FilterRowCriteriaProperties createAndInit(String name) {
                    return (FilterRowCriteriaProperties) new FilterRowCriteriaProperties(name).init();
                }

            });

    public transient ISchemaListener schemaListener;

    public FilterRowProperties(String name) {
        super(name);
        filters.init();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(filters).setWidgetType(Widget.NESTED_PROPERTIES)
                .setConfigurationValue(Widget.NESTED_PROPERTIES_TYPE_OPTION, "filter"));
        mainForm.addRow(Widget.widget(logicalOp).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        setupLayout();
        // all conditions required
        logicalOp.setValue(LogicalOpType.ALL);
        // Add a default filter criteria
        filters.createAndAddRow();

        schemaListener = new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateOutputSchemas();
                updateConditionsRow();
            }

        };
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            updateConditionsRow();
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new LinkedHashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            // output schemas
            connectors.add(FLOW_CONNECTOR);
            connectors.add(REJECT_CONNECTOR);
        } else {
            // input schema
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    protected void updateOutputSchemas() {
        // Copy the "main" schema into the "flow" schema
        Schema inputSchema = main.schema.getValue();
        schemaFlow.schema.setValue(inputSchema);

        // Copy the "main" schema in to the "reject" schema
        schemaReject.schema.setValue(inputSchema);
    }

    /**
     * TODO: This method will be used once trigger will be implemented on TFD UI
     */
    private void updateOperatorColumn() {
        Collection<FilterRowCriteriaProperties> criterias = filters.getPropertiesList();
        for (FilterRowCriteriaProperties filterRowCriteriaProperties : criterias) {
            filterRowCriteriaProperties.updateOperatorColumn();
        }
    }

    /**
     * TODO: This method will be used once the field autocompletion will be implemented
     */
    private void updateFunctionColumn() {
        Collection<FilterRowCriteriaProperties> criterias = filters.getPropertiesList();
        for (FilterRowCriteriaProperties filterRowCriteriaProperties : criterias) {
            filterRowCriteriaProperties.updateFuntionColumn();
        }

        // Finally check the operator
        updateOperatorColumn();
    }

    /**
     * TODO: This method will be used once the field autocompletion will be implemented
     */
    protected void updateConditionsRow() {
        updateFunctionColumn();
    }

    /**
     * TODO: This method will be used once the field autocompletion will be implemented
     */
    private Boolean isString(Schema.Type type) {
        return Schema.Type.STRING.equals(type);
    }

    /**
     * TODO: This method will be used once the field autocompletion will be implemented
     */
    private Boolean isNumerical(Schema.Type type) {
        return Schema.Type.INT.equals(type) || Schema.Type.LONG.equals(type) //
                || Schema.Type.DOUBLE.equals(type) || Schema.Type.FLOAT.equals(type);
    }
}
