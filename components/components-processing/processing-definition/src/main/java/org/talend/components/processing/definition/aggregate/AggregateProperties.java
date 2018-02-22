package org.talend.components.processing.definition.aggregate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.PropertiesList;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class AggregateProperties extends FixedConnectorsComponentProperties {

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public transient PropertyPathConnector FLOW_CONNECTOR =
            new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties main = new SchemaProperties("main");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public PropertiesList<AggregateGroupByProperties> groupBy =
            new PropertiesList<>("groupBy", new PropertiesList.NestedPropertiesFactory<AggregateGroupByProperties>() {

                @Override
                public AggregateGroupByProperties createAndInit(String name) {
                    return (AggregateGroupByProperties) new AggregateGroupByProperties(name).init();
                }
            });

    public PropertiesList<AggregateOperationProperties> operations = new PropertiesList<>("operations",
            new PropertiesList.NestedPropertiesFactory<AggregateOperationProperties>() {

                @Override
                public AggregateOperationProperties createAndInit(String name) {
                    return (AggregateOperationProperties) new AggregateOperationProperties(name).init();
                }
            });

    public List<AggregateGroupByProperties> filteredGroupBy() {
        List<AggregateGroupByProperties> filteredGroupBy = new ArrayList<>();
        for (AggregateGroupByProperties groupProps : groupBy.getPropertiesList()) {
            String fieldPath = groupProps.fieldPath.getValue();
            if (StringUtils.isEmpty(fieldPath)) {
                continue;
            }
            // TODO the incoming field name will start with ".", it's for avpath, consider it after
            if (fieldPath.startsWith(".")) {
                groupProps.fieldPath.setValue(fieldPath.substring(1));
            }
            filteredGroupBy.add(groupProps);
        }
        return filteredGroupBy;
    }

    public List<AggregateOperationProperties> filteredOperations() {
        List<AggregateOperationProperties> filteredOperations = new ArrayList<>();
        for (AggregateOperationProperties funcProps : operations.getPropertiesList()) {
            String fieldPath = funcProps.fieldPath.getValue();
            if (StringUtils.isEmpty(fieldPath)) {
                continue;
            }
            // TODO the incoming field name will start with ".", it's for avpath, consider it after
            if (fieldPath.startsWith(".")) {
                funcProps.fieldPath.setValue(fieldPath.substring(1));
            }
            // TODO consider avpath for funcProps.outputFieldPath?
            filteredOperations.add(funcProps);
        }
        return filteredOperations;
    }

    public AggregateProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);

        mainForm.addRow(Widget.widget(groupBy).setWidgetType(Widget.UNCOLLAPSIBLE_NESTED_PROPERTIES)
                .setConfigurationValue(Widget.NESTED_PROPERTIES_TYPE_OPTION, "groupment parameter"));
        mainForm.addRow(Widget.widget(operations).setWidgetType(Widget.COLUMNS_PROPERTIES)
                .setConfigurationValue(Widget.NESTED_PROPERTIES_TYPE_OPTION, "operation"));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        groupBy.init();
        groupBy.setMinItems("0");
        groupBy.createAndAddRow();
        operations.init();
        operations.createAndAddRow();
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
}
