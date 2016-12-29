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
package org.talend.components.kafka.runtime;

import java.io.ByteArrayOutputStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaOutputPTransformRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<KafkaOutputProperties> {

    private static Logger LOG = LoggerFactory.getLogger(KafkaOutputPTransformRuntime.class);

    private KafkaOutputProperties properties;

    @Override
    public PDone expand(PCollection<IndexedRecord> objectPCollection) {
        final boolean useAvro = properties.getDatasetProperties().valueFormat
                .getValue() == KafkaDatasetProperties.ValueFormat.AVRO;
        final boolean useSimpleAvro = !properties.getDatasetProperties().isHierarchy.getValue();

        PCollection<KV<byte[], byte[]>> kafkaCollection = objectPCollection.apply("ExtractIndexedRecord",
                ParDo.of(new DoFn<IndexedRecord, KV<byte[], byte[]>>() {

                    @DoFn.ProcessElement
                    public void processElement(ProcessContext c) throws Exception {
                        // FIXME auto convert type before here, or use converter with some built-in auto convert
                        // function for basic type
                        byte[] value = null;
                        Schema schema = c.element().getSchema();
                        if (useAvro) {

                            DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(
                                    properties.getDatasetProperties().getValueAvroSchema());
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

                            GenericRecord payload = null;
                            if (useSimpleAvro) {
                                payload = new GenericData.Record(properties.getDatasetProperties().getValueAvroSchema());
                                for (Schema.Field field : payload.getSchema().getFields()) {
                                    // put the value in incoming record to the intermediate avro payload
                                    Object v = c.element().get(schema.getField(field.name()).pos());
                                    payload.put(field.name(), v);
                                }
                            } else {
                                payload = (GenericRecord) c.element().get(schema.getField("value").pos());
                            }

                            datumWriter.write(payload, encoder);
                            encoder.flush();
                            out.close();
                            value = out.toByteArray();
                        } else {
                            value = (byte[]) c.element().get(schema.getField("value").pos());
                        }
                        c.output(KV.of((byte[]) c.element().get(schema.getField("key").pos()), value));
                    }
                }));

        return kafkaCollection.apply(KafkaIO.write()
                .withBootstrapServers(properties.getDatasetProperties().getDatastoreProperties().brokers.getValue())
                .withTopic(properties.getDatasetProperties().topic.getValue()).withKeyCoder(ByteArrayCoder.of())
                .withValueCoder(ByteArrayCoder.of()).updateProducerProperties(KafkaConnection.createOutputMaps(properties)));

    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaOutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

}
