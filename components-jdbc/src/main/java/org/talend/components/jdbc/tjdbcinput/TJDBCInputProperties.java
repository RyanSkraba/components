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
package org.talend.components.jdbc.tjdbcinput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.Collections;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.module.JDBCTableSelectionModule;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.sqlbuilder.JDBCSQLBuilder;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCInputProperties extends FixedConnectorsComponentProperties
        implements ComponentReferencePropertiesEnclosing, RuntimeSettingProvider {

    public TJDBCInputProperties(String name) {
        super(name);
    }

    // main
    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public final PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public SchemaProperties main = new SchemaProperties("main");

    public JDBCTableSelectionModule tableSelection = new JDBCTableSelectionModule("tableSelection");

    // TODO query type

    public Property<String> sql = PropertyFactory.newString("sql").setRequired(true);

    public final PresentationItem fetchSchemaFromQuery = new PresentationItem("fetchSchemaFromQuery", "Fetch schema from query");

    public final PresentationItem guessQueryFromSchema = new PresentationItem("guessQueryFromSchema", "Guess query from schema");

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    // advanced
    public Property<Boolean> useCursor = PropertyFactory.newBoolean("useCursor").setRequired();

    public Property<Integer> cursor = PropertyFactory.newInteger("cursor").setRequired();

    public Property<Boolean> trimStringOrCharColumns = PropertyFactory.newBoolean("trimStringOrCharColumns").setRequired();

    // TODO the tirm table

    // TODO enable mapping for dynamic

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        referencedComponent.componentType.setValue(TJDBCConnectionDefinition.COMPONENT_NAME);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(tableSelection.getForm(Form.REFERENCE));
        mainForm.addRow(sql);

        mainForm.addRow(Widget.widget(fetchSchemaFromQuery).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);

        // TODO now there is some issue about the layout, have to write here
        mainForm.addRow(Widget.widget(guessQueryFromSchema).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(useCursor);
        advancedForm.addRow(cursor);
        advancedForm.addRow(trimStringOrCharColumns);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        // TODO fix it later
        // sql.setValue("select id, name from employee");

        cursor.setValue(1000);

        tableSelection.setConnection(this);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = referencedComponent.componentInstanceId.getStringValue();
        boolean useOtherConnection = refComponentIdValue != null
                && refComponentIdValue.startsWith(TJDBCConnectionDefinition.COMPONENT_NAME);

        if (form.getName().equals(Form.MAIN)) {
            form.getChildForm(connection.getName()).setHidden(useOtherConnection);
            if (useOtherConnection) {
                form.getWidget(useDataSource.getName()).setHidden(true);
                form.getWidget(dataSource.getName()).setHidden(true);
            } else {
                form.getWidget(useDataSource.getName()).setHidden(false);
                form.getWidget(dataSource.getName()).setHidden(!useDataSource.getValue());
            }
        }

        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(cursor.getName()).setHidden(!useCursor.getValue());
        }
    }

    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseDataSource() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseCursor() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(mainConnector);
        }
        return Collections.emptySet();
    }

    public ValidationResult afterFetchSchemaFromQuery() {
        JDBCSourceOrSink ss = new JDBCSourceOrSink();
        ss.initialize(null, this);
        Schema schema = null;
        try {
            schema = ss.getSchemaFromQuery(null, sql.getValue());
        } catch (Exception e) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage(e.getCause().getMessage());
        }
        main.schema.setValue(schema);
        return ValidationResult.OK;
    }

    public ValidationResult afterGuessQueryFromSchema() {
        String tablename = tableSelection.tablename.getValue();
        Schema schema = main.schema.getValue();
        if (tablename == null || tablename.isEmpty()) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage("Please set the table name before it");
        }
        if (schema == null || schema.getFields().isEmpty()) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage("Please set the schema before it");
        }
        String query = JDBCSQLBuilder.getInstance().generateSQL4SelectTable(tablename, schema);
        sql.setValue("\"" + query + "\"");

        // TODO : it doesn't work
        refreshLayout(getForm(Form.MAIN));

        return ValidationResult.OK;
    }

    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        setting.setReferencedComponentId(referencedComponent.componentInstanceId.getValue());
        setting.setReferencedComponentProperties(referencedComponent.componentProperties);

        CommonUtils.setCommonConnectionInfo(setting, connection);

        setting.setTablename(this.tableSelection.tablename.getValue());
        setting.setSql(this.sql.getValue());

        setting.setUseCursor(this.useCursor.getValue());
        setting.setCursor(this.cursor.getValue());
        setting.setTrimStringOrCharColumns(this.trimStringOrCharColumns.getValue());

        setting.setSchema(main.schema.getValue());

        return setting;
    }

}
