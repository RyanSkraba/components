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
package org.talend.components.jdbc.dataprep;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.dataset.JDBCDatasetDefinition;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.ReferenceProperties;

public class JDBCInputProperties extends FixedConnectorsComponentProperties
        implements IOProperties<JDBCDatasetProperties>, RuntimeSettingProvider {

    public transient ReferenceProperties<JDBCDatasetProperties> dataset = new ReferenceProperties<>("dataset",
            JDBCDatasetDefinition.NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public JDBCInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public JDBCDatasetProperties getDatasetProperties() {
        return dataset.getReference();
    }

    @Override
    public void setDatasetProperties(JDBCDatasetProperties datasetProperties) {
        dataset.setReference(datasetProperties);
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        JDBCDatasetProperties datasetProperties = this.getDatasetProperties();
        JDBCDatastoreProperties datastoreProperties = datasetProperties.getDatastoreProperties();

        setting.setDriverPaths(datastoreProperties.getCurrentDriverPaths());
        setting.setDriverClass(datastoreProperties.getCurrentDriverClass());
        setting.setJdbcUrl(datastoreProperties.jdbcUrl.getValue());

        setting.setUsername(datastoreProperties.userId.getValue());
        setting.setPassword(datastoreProperties.password.getValue());

        setting.setSql(datasetProperties.sql.getValue());

        setting.setSchema(datasetProperties.main.schema.getValue());

        return setting;
    }
}
