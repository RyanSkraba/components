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
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Test;
import org.talend.components.jms.input.JmsInputProperties;
import org.talend.components.jms.runtime_1_1.JmsInputPTransformRuntime;

import java.util.Arrays;
import java.util.HashSet;

public class JmsInputPTransformRuntimeTestIT {

    @Test
    public void test() {

        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

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
        JmsInputProperties inputProperties = new JmsInputProperties("input");
        inputProperties.setValue("timeout", 10000);
        inputProperties.setValue("max_msg", 6);
        inputProperties.datasetRef.setReference(datasetProps);
        JmsInputPTransformRuntime input = new JmsInputPTransformRuntime();
        input.initialize(null, inputProperties);
        input.setMessageType();

        PCollection<String> test = input.apply(p.begin());

        PAssert.that(test).containsInAnyOrder(new HashSet<>(Arrays.asList("hi", "ha", "ho", "there", "sue", "bob")));

        p.run();
    }
}
