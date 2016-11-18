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

import java.util.Arrays;
import java.util.List;

public class JmsOutputPTransformRuntimeTestIT {

    static final Object[] WORDS_ARRAY = new String[] { "hi", "there", "ho", "ha", "sue", "bob" };

    static final List<Object> WORDS = Arrays.asList(WORDS_ARRAY);

    @Test
    public void test() {
        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

        PCollection<Object> input = p.apply(Create.of(WORDS));

        // configure datastore
        JmsDatastoreProperties datastoreProps = new JmsDatastoreProperties("datastoreProps");
        datastoreProps.setValue("version", JmsDatastoreProperties.JmsVersion.V_1_1);
        datastoreProps.setValue("contextProvider", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        datastoreProps.setValue("serverUrl", "tcp://localhost:61616");
        datastoreProps.setValue("connectionFactoryName", "ConnectionFactory");

        // configure dataset
        JmsDatasetProperties datasetProps = new JmsDatasetProperties("datasetProps");
        datasetProps.setValue("queueTopicName", "Consumer");
        datasetProps.setValue("msgType", JmsMessageType.QUEUE);
        datasetProps.datastoreRef.setReference(datastoreProps);

        // configure output
        JmsOutputProperties outputProperties = new JmsOutputProperties("output");
        outputProperties.setValue("delivery_mode", JmsOutputProperties.JmsAdvancedDeliveryMode.PERSISTENT);
        outputProperties.datasetRef.setReference(datasetProps);
        JmsOutputPTransformRuntime output = new JmsOutputPTransformRuntime();
        output.initialize(null, outputProperties);
        output.setMessageType();

        output.apply(input);

        p.run();

    }
}
