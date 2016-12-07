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

package org.talend.components.jdbc.datastream;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class JDBCOutputProperties extends FixedConnectorsComponentProperties
        implements IOProperties<JDBCDatasetProperties> {

    public ReferenceProperties<JDBCDatasetProperties> dataset = new ReferenceProperties<>("dataset",
            JDBCOutputDefinition.NAME);

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

    // missing part for JdbcIO, can not run multiple sql same time, so do not support insertOrUpdate/updateOrInsert
    public enum DataAction {
        INSERT,
        UPDATE,
        DELETE
    }
}
