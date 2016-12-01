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
package org.talend.components.jdbc.dataset;

import org.apache.avro.Schema;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class JDBCDatasetProperties extends PropertiesImpl
        implements DatasetProperties<JDBCDatastoreProperties>, RuntimeSettingProvider {

    public transient ReferenceProperties<JDBCDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            JDBCDatastoreDefinition.NAME);

    public Property<String> sql = PropertyFactory.newString("sql").setRequired(true);

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void beforeSchema() {
            updateSchema();
        }

    };

    public void updateSchema() {
        JDBCDatasetDefinition definition = new JDBCDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this, null);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            JDBCDatasetRuntime runtime = (JDBCDatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            Schema schema = runtime.getSchema();
            main.schema.setValue(schema);
        }
    }

    public JDBCDatasetProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        sql.setValue("\"select * from mytable\"");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);
        mainForm.addRow(sql);

        mainForm.addRow(main.getForm(Form.REFERENCE));
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
        AllSetting setting = new AllSetting();

        JDBCDatastoreProperties datastoreProperties = this.getDatastoreProperties();
        setting.setDriverPaths(datastoreProperties.getCurrentDriverPaths());
        setting.setDriverClass(datastoreProperties.getCurrentDriverClass());
        setting.setJdbcUrl(datastoreProperties.jdbcUrl.getValue());

        setting.setUsername(datastoreProperties.userId.getValue());
        setting.setPassword(datastoreProperties.password.getValue());

        setting.setSchema(main.schema.getValue());

        setting.setSql(sql.getValue());

        return setting;
    }

}
