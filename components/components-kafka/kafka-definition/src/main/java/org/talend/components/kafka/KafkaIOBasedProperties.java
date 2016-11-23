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
package org.talend.components.kafka;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.kafka.dataset.KafkaDatasetDefinition;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.daikon.properties.ReferenceProperties;

public abstract class KafkaIOBasedProperties extends FixedConnectorsComponentProperties
        implements IOProperties<KafkaDatasetProperties> {

    public transient ReferenceProperties<KafkaDatasetProperties> dataset = new ReferenceProperties<>("dataset",
            KafkaDatasetDefinition.NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "module.main");

    public KafkaIOBasedProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        // Form mainForm = new Form(this, Form.MAIN);
        // For datastream, do not contains the property in datastore and dataset
        // For studio, should contains the property in datastore and dataset
        // mainForm.addRow(dataset.getDatastoreProperties().getForm(Form.REFERENCE));
        // mainForm.addRow(dataset.getForm(Form.MAIN));
    }

    @Override
    public KafkaDatasetProperties getDatasetProperties() {
        return dataset.getReference();
    }

    @Override
    public void setDatasetProperties(KafkaDatasetProperties datasetProperties) {
        dataset.setReference(datasetProperties);
    }

}
