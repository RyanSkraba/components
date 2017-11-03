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

package org.talend.components.jms.input;

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

public class JmsInputProperties extends FixedConnectorsComponentProperties implements IOProperties {

    public JmsInputProperties(String name) {
        super(name);
    }

    /** Hidden property used to specify that this component generates unbounded input. */
    public Property<Boolean> isStreaming = PropertyFactory.newBoolean("isStreaming", true);

    public Property<Integer> timeout = PropertyFactory.newInteger("timeout", -1);

    public Property<Integer> max_msg = PropertyFactory.newInteger("max_msg", -1);

    public Property<String> msg_selector = PropertyFactory.newString("msg_selector", "");

    public transient ReferenceProperties<JmsDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            JmsDatasetDefinition.NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "datasetRef.main");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(timeout);
        mainForm.addRow(max_msg);
        mainForm.addRow(msg_selector);
        mainForm.addRow(isStreaming);
        mainForm.getWidget(isStreaming).setHidden();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
        return connectors;
    }

    @Override
    public DatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }
}
