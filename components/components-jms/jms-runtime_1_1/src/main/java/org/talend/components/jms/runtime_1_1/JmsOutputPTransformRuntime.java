package org.talend.components.jms.runtime_1_1;

import org.apache.avro.generic.IndexedRecord;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.jms.JmsIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;

import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jms.JmsMessageType;
import org.talend.components.jms.output.JmsOutputProperties;

import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.exception.TalendRuntimeException;

public class JmsOutputPTransformRuntime extends PTransform<PCollection<Object>, PDone>
        implements RuntimableRuntime {

    private JmsOutputProperties properties;

    private JmsMessageType messageType;

    @Override public PDone apply(PCollection<Object> objectPCollection) {
        System.out.println("deb apply");
        PCollection<IndexedRecord> test = objectPCollection.apply("ExtractIndexedRecord", ParDo.of(new DoFn<Object, IndexedRecord>() {
            IndexedRecordConverter converter;
            transient AvroRegistry avroRegistry = new AvroRegistry();
            @DoFn.ProcessElement public void processElement(ProcessContext c) throws Exception {
                if (c.element() == null){
                    return;
                }
                if (converter == null){
                    converter = avroRegistry.createIndexedRecordConverter(c.element().getClass());
                    c.output((IndexedRecord)converter.convertToAvro(c.element()));
                }
            }
        }));
        //test.setCoder(AvroCoder.of(IndexedRecord.class));

        PCollection<String> jmsCollection = test.apply("ExtractString", ParDo.of(new DoFn<IndexedRecord, String>() {
            @DoFn.ProcessElement public void processElement(ProcessContext c) throws Exception {
                c.output(c.element().toString());
            }
        }));

        if (messageType.equals(JmsMessageType.QUEUE)) {
            return jmsCollection.apply(JmsIO.write()
                    //.withConnectionFactory(properties.dataset.datastore.getConnectionFactory())
                    .withQueue(properties.to.toString()));
        } else if (messageType.equals(JmsMessageType.TOPIC)) {
            // TODO label comes from user
           return jmsCollection.apply("writeToJms", JmsIO.write()
                    //.withConnectionFactory(properties.dataset.datastore.getConnectionFactory())
                    .withTopic(properties.to.toString()));
        } else {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_ARGUMENT);
        }
    }

    @Override public ValidationResult initialize(RuntimeContainer container, Properties properties) {
        this.properties = (JmsOutputProperties) properties;
        return ValidationResult.OK;    }

    public void setMessageType(){
        messageType = properties.dataset.msgType.getValue();
    }
}
