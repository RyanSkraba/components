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

package org.talend.components.jdbc.datastream;

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
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JDBCOutputProperties extends FixedConnectorsComponentProperties
        implements IOProperties<JDBCDatasetProperties>, RuntimeSettingProvider {

    public ReferenceProperties<JDBCDatasetProperties> dataset = new ReferenceProperties<>("dataset", JDBCDatasetDefinition.NAME);

    public Property<DataAction> dataAction = PropertyFactory.newEnum("dataAction", DataAction.class).setRequired();

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "module.main");

    public JDBCOutputProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form main = new Form(this, Form.MAIN);
        main.addRow(dataAction);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        dataAction.setValue(DataAction.INSERT);
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
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            return Collections.EMPTY_SET;
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public AllSetting getRuntimeSetting() {
        // TODO only return the Driver paths is enough to get the getRuntimeInfo works, AllSetting should be removed
        // after refactor
        AllSetting setting = new AllSetting();

        JDBCDatasetProperties datasetProperties = this.getDatasetProperties();
        JDBCDatastoreProperties datastoreProperties = datasetProperties.getDatastoreProperties();

        setting.setDriverPaths(datastoreProperties.getCurrentDriverPaths());
        setting.setDriverClass(datastoreProperties.getCurrentDriverClass());
        setting.setJdbcUrl(datastoreProperties.jdbcUrl.getValue());

        setting.setUsername(datastoreProperties.userId.getValue());
        setting.setPassword(datastoreProperties.password.getValue());

        return setting;
    }

    // missing part for JdbcIO, can not run multiple sql same time, so do not support insertOrUpdate/updateOrInsert
    public enum DataAction {
        INSERT,
        UPDATE,
        DELETE
    }
}
