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

package org.talend.components.kinesis.input;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.kinesis.KinesisDatasetDefinition;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class KinesisInputProperties extends FixedConnectorsComponentProperties implements IOProperties {

    public ReferenceProperties<KinesisDatasetProperties> datasetRef =
            new ReferenceProperties<>("datasetRef", KinesisDatasetDefinition.NAME);

    public Property<OffsetType> position =
            PropertyFactory.newEnum("position", OffsetType.class).setValue(OffsetType.LATEST).setRequired();

    public Property<Boolean> useMaxReadTime = PropertyFactory.newBoolean("useMaxReadTime", false);

    // Max duration(Millions) from start receiving
    public Property<Long> maxReadTime = PropertyFactory.newProperty(Long.class, "maxReadTime").setValue(600000L);

    public Property<Boolean> useMaxNumRecords = PropertyFactory.newBoolean("useMaxNumRecords", false);

    public Property<Integer> maxNumRecords = PropertyFactory.newProperty(Integer.class, "maxNumRecords").setValue(5000);

    protected transient PropertyPathConnector MAIN_CONNECTOR =
            new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public KinesisInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(position);
        mainForm.addRow(useMaxReadTime).addColumn(maxReadTime);
        mainForm.addRow(useMaxNumRecords).addColumn(maxNumRecords);
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
            maxReadTime.setRequired(useMaxReadTime.getValue());
            form.getWidget(maxNumRecords).setVisible(useMaxNumRecords);
            maxNumRecords.setRequired(useMaxNumRecords.getValue());
        }
    }

    @Override
    public KinesisDatasetProperties getDatasetProperties() {
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

    // sync with org.apache.kafka.clients.consumer.OffsetResetStrategy
    public enum OffsetType {
        LATEST,
        EARLIEST
    }
}
