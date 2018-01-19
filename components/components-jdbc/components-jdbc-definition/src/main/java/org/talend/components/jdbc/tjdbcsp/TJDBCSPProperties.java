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
package org.talend.components.jdbc.tjdbcsp;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.module.SPParameterTable;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCSPProperties extends FixedConnectorsComponentProperties implements RuntimeSettingProvider {

    public TJDBCSPProperties(String name) {
        super(name);
    }

    // main
    public ComponentReferenceProperties<TJDBCConnectionProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", TJDBCConnectionDefinition.COMPONENT_NAME);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void afterSchema() {
            updateOutputSchemas();
            updateReturnResultIn();
            updateSpParameterTable();
        }

    };

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public Property<String> spName = PropertyFactory.newProperty("spName").setRequired();

    public Property<Boolean> isFunction = PropertyFactory.newBoolean("isFunction").setRequired();

    public Property<String> returnResultIn = PropertyFactory.newProperty("returnResultIn").setRequired();

    public SPParameterTable spParameterTable = new SPParameterTable("spParameterTable");

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    public void updateOutputSchemas() {
        Schema inputSchema = main.schema.getValue();
        schemaFlow.schema.setValue(inputSchema);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));

        mainForm.addRow(spName);
        mainForm.addRow(isFunction);
        mainForm.addRow(widget(returnResultIn).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addRow(widget(spParameterTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));

        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        
        connection.setNotRequired();
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        boolean useOtherConnection = CommonUtils.useExistedConnection(referencedComponent);

        if (form.getName().equals(Form.MAIN)) {
            form.getChildForm(connection.getName()).setHidden(useOtherConnection);
            if (useOtherConnection) {
                form.getWidget(useDataSource.getName()).setHidden(true);
                form.getWidget(dataSource.getName()).setHidden(true);
            } else {
                form.getWidget(useDataSource.getName()).setHidden(false);
                form.getWidget(dataSource.getName()).setHidden(!useDataSource.getValue());
            }

            form.getWidget(returnResultIn.getName()).setVisible(isFunction.getValue());
        }
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseDataSource() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterIsFunction() {
        refreshLayout(getForm(Form.MAIN));
    }

    private void updateReturnResultIn() {
        returnResultIn.setPossibleValues(CommonUtils.getAllSchemaFieldNames(main.schema.getValue()));
    }

    private void updateSpParameterTable() {
        spParameterTable.schemaColumns.setPossibleValues(CommonUtils.getAllSchemaFieldNames(main.schema.getValue()));
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(FLOW_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        CommonUtils.setReferenceInfo(setting, referencedComponent);

        CommonUtils.setCommonConnectionInfo(setting, connection);

        setting.setSpName(spName.getValue());
        setting.setIsFunction(isFunction.getValue());
        setting.setReturnResultIn(returnResultIn.getValue());

        setting.setSchemaColumns4SPParameters(spParameterTable.schemaColumns.getValue());
        setting.setParameterTypes(spParameterTable.parameterTypes.getValue());

        setting.setUseDataSource(this.useDataSource.getValue());
        setting.setDataSource(this.dataSource.getValue());

        return setting;
    }

}
