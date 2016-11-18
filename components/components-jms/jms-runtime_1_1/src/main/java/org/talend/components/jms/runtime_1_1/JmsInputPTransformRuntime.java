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

package org.talend.components.jms.runtime_1_1;

import org.apache.beam.sdk.io.jms.JmsIO;
import org.apache.beam.sdk.io.jms.JmsRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jms.JmsMessageType;
import org.talend.components.jms.input.JmsInputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.joda.time.Duration;

public class JmsInputPTransformRuntime extends PTransform<PBegin, PCollection> implements RuntimableRuntime {

    transient private JmsInputProperties properties;

    private JmsDatastoreRuntime datastoreRuntime;

    private JmsMessageType messageType;

    @Override
    public ValidationResult initialize(RuntimeContainer container, Properties properties) {
        this.properties = (JmsInputProperties) properties;
        return ValidationResult.OK;
    }

    public void setMessageType() {
        messageType = properties.datasetRef.getReference().msgType.getValue();
    }

    public JmsMessageType getMessageType() {
        return messageType;
    }

    @Override
    public PCollection apply(PBegin pBegin) {

        datastoreRuntime = new JmsDatastoreRuntime();
        datastoreRuntime.initialize(null, properties.datasetRef.getReference().getDatastoreProperties());

        JmsIO.Read read = JmsIO.read().withConnectionFactory(datastoreRuntime.getConnectionFactory());
        if (messageType.equals(JmsMessageType.QUEUE)) {
            read = read.withQueue(properties.datasetRef.getReference().queueTopicName.getValue());
        } else if (messageType.equals(JmsMessageType.TOPIC)) {
            read = read.withTopic(properties.datasetRef.getReference().queueTopicName.getValue());
        }

        if (properties.max_msg.getValue() != -1 && properties.timeout.getValue() != -1) {
            read = read.withMaxNumRecords(properties.max_msg.getValue())
                    .withMaxReadTime(Duration.millis(properties.timeout.getValue()));
        } else if (properties.max_msg.getValue() != -1) {
            read = read.withMaxNumRecords(properties.max_msg.getValue());
        } else if (properties.timeout.getValue() != -1) {
            read = read.withMaxReadTime(Duration.millis(properties.timeout.getValue()));
        }
        PCollection<JmsRecord> jmsCollection = pBegin.apply("ReadFromJms", read);

        if (jmsCollection != null) {
            PCollection<String> outputCollection = jmsCollection.apply("ExtractString", ParDo.of(new DoFn<JmsRecord, String>() {

                @DoFn.ProcessElement
                public void processElement(ProcessContext c) throws Exception {
                    c.output(c.element().getPayload());
                }
            }));
            return outputCollection;
        }
        return null;
    }
}
