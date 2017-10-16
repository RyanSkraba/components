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
package org.talend.components.jdbc.dataset;

import org.apache.avro.Schema;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class JDBCDatasetProperties extends PropertiesImpl
        implements DatasetProperties<JDBCDatastoreProperties>, RuntimeSettingProvider {

    public ReferenceProperties<JDBCDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            JDBCDatastoreDefinition.NAME);

    public Property<SourceType> sourceType = PropertyFactory.newEnum("sourceType", SourceType.class);

    public Property<String> tableName = PropertyFactory.newString("tableName");

    public Property<String> sql = PropertyFactory.newString("sql");

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void beforeSchema() {
            updateSchema();
        }

    };

    private transient AllSetting setting;

    public JDBCDatasetProperties(String name) {
        super(name);
        setting = new AllSetting();
    }

    public void afterSourceType() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void updateSchema() {
        JDBCDatasetDefinition definition = new JDBCDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            DatasetRuntime<JDBCDatasetProperties> runtime = (DatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            Schema schema = runtime.getSchema();
            main.schema.setValue(schema);
        }
    }

    @Override
    public void setupProperties() {
        sourceType.setValue(SourceType.QUERY);
        sql.setValue("select * from mytable");
    }

    @Override
    public void setupLayout() {
        Form mainForm = CommonUtils.addForm(this, Form.MAIN);
        mainForm.addRow(sourceType);
        mainForm.addRow(tableName);
        mainForm.addRow(Widget.widget(sql).setWidgetType(Widget.CODE_WIDGET_TYPE)
                .setConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF, "sql"));

        Form citizenUserForm = CommonUtils.addForm(this, Form.CITIZEN_USER);
        citizenUserForm.addRow(Widget.widget(sql).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));

        // citizenUserForm.addColumn(widget(main).setHidden(true));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getWidget(tableName) != null) {
            form.getWidget(tableName).setVisible(sourceType.getValue() == SourceType.TABLE_NAME);
        }

        form.getWidget(sql).setVisible(sourceType.getValue() == SourceType.QUERY);
    }

    @Override
    public JDBCDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(JDBCDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
    }

    @Override
    public AllSetting getRuntimeSetting() {

        JDBCDatastoreProperties datastoreProperties = this.getDatastoreProperties();
        setting.setDriverPaths(datastoreProperties.getCurrentDriverPaths());
        setting.setDriverClass(datastoreProperties.getCurrentDriverClass());
        setting.setJdbcUrl(datastoreProperties.jdbcUrl.getValue());

        setting.setUsername(datastoreProperties.userId.getValue());
        setting.setPassword(datastoreProperties.password.getValue());

        setting.setSchema(main.schema.getValue());

        setting.setSql(getSql());
        if (sourceType.getValue() == SourceType.TABLE_NAME) {
            setting.setTablename(tableName.getValue());
        }

        return setting;
    }

    public String getSql() {
        if (sourceType.getValue() == SourceType.TABLE_NAME) {
            return "select * from " + tableName.getValue();
        } else {
            return sql.getValue();
        }
    }

    public enum SourceType {
        TABLE_NAME,
        QUERY
    }

}
