package org.talend.components.jms;

import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Test;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.jms.output.JmsOutputProperties;
import org.talend.components.jms.runtime_1_1.JmsOutputPTransformRuntime;

public class JmsOutputPTransformRuntimeTest {

    @Test
    public void test() {
/*
        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

        PCollection<Object> input = p.apply(Create.of((Object)"MyString1", "MyString2"));

        // configure datastore
        JmsDatastoreProperties datastoreProps = new JmsDatastoreProperties("datastoreProps");
        datastoreProps.setValue("version", JmsDatastoreProperties.JmsVersion.V_1_1);
        datastoreProps.setValue("contextProvider", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        datastoreProps.setValue("serverUrl","tcp://localhost:61616");
        datastoreProps.setValue("connectionFactoryName","ConnectionFactory");

        // configure dataset
        JmsDatasetProperties datasetProps = new JmsDatasetProperties("datasetProps");
        datasetProps.setValue("msgType", JmsMessageType.QUEUE);
        datasetProps.datastore = datastoreProps;
        //datasetProps.setValue("datastore", datastoreProps);

        // configure output
        JmsOutputProperties outputProperties = new JmsOutputProperties("output");
        outputProperties.setValue("to","testQueue");
        outputProperties.setValue("delivery_mode", JmsOutputProperties.JmsAdvancedDeliveryMode.persistent);
        outputProperties.dataset = datasetProps;
        //outputProperties.setValue("dataset",datasetProps);
        //outputProperties.init();
        JmsOutputPTransformRuntime output = new JmsOutputPTransformRuntime();
        output.initialize(null, outputProperties);
        output.setMessageType();

        input.apply(output);
        p.run();
        */
    }
}
