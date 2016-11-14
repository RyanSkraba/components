package org.talend.components.kafka;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;

public abstract class KafkaIOBasedProperties extends FixedConnectorsComponentProperties
        implements IOProperties<KafkaDatasetProperties> {

    public transient KafkaDatasetProperties dataset = new KafkaDatasetProperties("dataset");

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
        return dataset;
    }

    @Override
    public void setDatasetProperties(KafkaDatasetProperties datasetProperties) {
        dataset = datasetProperties;
    }

}
