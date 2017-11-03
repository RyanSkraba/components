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

package org.talend.components.pubsub.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.pubsub.PubSubDatasetDefinition;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.runtime.IPubSubDatasetRuntime;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class PubSubInputProperties extends FixedConnectorsComponentProperties implements IOProperties {

    public ReferenceProperties<PubSubDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            PubSubDatasetDefinition.NAME);

    /** Hidden property used to specify that this component generates unbounded input. */
    public Property<Boolean> isStreaming = PropertyFactory.newBoolean("isStreaming", true);

    public Property<Boolean> useMaxReadTime = PropertyFactory.newBoolean("useMaxReadTime", false);

    // Max duration(Millions) from start receiving
    public Property<Long> maxReadTime = PropertyFactory.newProperty(Long.class, "maxReadTime");

    public Property<Boolean> useMaxNumRecords = PropertyFactory.newBoolean("useMaxNumRecords", false);

    public Property<Integer> maxNumRecords = PropertyFactory.newProperty(Integer.class, "maxNumRecords");

    public Property<String> idLabel = PropertyFactory.newString("idLabel");

    public Property<String> timestampLabel = PropertyFactory.newString("timestampLabel");

    // This property only used for getSample, the value only be changed internal
    public Property<Boolean> noACK = PropertyFactory.newBoolean("noACK", false);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public PubSubInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        maxReadTime.setValue(600000L);
        maxNumRecords.setValue(5000);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(useMaxReadTime).addColumn(maxReadTime);
        mainForm.addRow(useMaxNumRecords).addColumn(maxNumRecords);
        mainForm.addRow(idLabel);
        mainForm.addRow(timestampLabel);
        mainForm.addRow(isStreaming);
        mainForm.getWidget(isStreaming).setHidden();
    }

    public void afterUseMaxReadTime() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseMaxNumRecords() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(maxReadTime).setVisible(useMaxReadTime);
            form.getWidget(maxNumRecords).setVisible(useMaxNumRecords);
        }
    }

    @Override
    public PubSubDatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
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
}
