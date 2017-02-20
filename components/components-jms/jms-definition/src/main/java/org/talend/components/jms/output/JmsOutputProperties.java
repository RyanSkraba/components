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

package org.talend.components.jms.output;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.jms.JmsDatasetDefinition;
import org.talend.components.jms.JmsDatasetProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class JmsOutputProperties extends FixedConnectorsComponentProperties implements IOProperties {

    public enum JmsAdvancedDeliveryMode {
        NON_PERSISTENT,
        PERSISTENT
    }

    public JmsOutputProperties(String name) {
        super(name);
    }

    public Property<JmsAdvancedDeliveryMode> delivery_mode = newEnum("delivery_mode", JmsAdvancedDeliveryMode.class)
            .setRequired();

    public Property<String> pool_max_total = PropertyFactory.newString("pool_max_total", "8");

    public Property<String> pool_max_wait = PropertyFactory.newString("pool_max_wait", "-1");

    public Property<String> pool_min_Idle = PropertyFactory.newString("pool_min_Idle", "0");

    public Property<String> pool_max_Idle = PropertyFactory.newString("pool_max_Idle", "8");

    public Property<Boolean> pool_use_eviction = newBoolean("pool_use_eviction", false);

    public Property<String> pool_time_between_eviction = PropertyFactory.newString("pool_time_between_eviction", "-1");

    public Property<String> pool_eviction_min_idle_time = PropertyFactory.newString("pool_eviction_min_idle_time", "1800000");

    public Property<String> pool_eviction_soft_min_idle_time = PropertyFactory.newString("pool_eviction_soft_min_idle_time", "0");

    public transient ReferenceProperties<JmsDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            JmsDatasetDefinition.NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "datasetRef.main");

    @Override
    public DatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(delivery_mode);
        advancedForm.addRow(pool_max_total);
        advancedForm.addRow(pool_max_wait);
        advancedForm.addRow(pool_min_Idle);
        advancedForm.addRow(pool_max_Idle);
        advancedForm.addRow(pool_use_eviction);
        advancedForm.addRow(pool_time_between_eviction);
        advancedForm.addRow(pool_eviction_min_idle_time);
        advancedForm.addRow(pool_eviction_soft_min_idle_time);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (Form.ADVANCED.equals(form.getName())) {
            form.getWidget(pool_time_between_eviction.getName()).setVisible(pool_use_eviction);
            form.getWidget(pool_eviction_min_idle_time.getName()).setVisible(pool_use_eviction);
            form.getWidget(pool_eviction_soft_min_idle_time.getName()).setVisible(pool_use_eviction);
        }
    }

    public void afterPool_use_eviction() {
        refreshLayout(getForm(Form.MAIN));
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

}
