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
package org.talend.components.kafka.runtime;

import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Sample;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

public class KafkaDatasetRuntime implements IKafkaDatasetRuntime {

    private KafkaDatasetProperties dataset;

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaDatasetProperties properties) {
        this.dataset = properties;
        return ValidationResult.OK;
    }

    @Override
    public Set<String> listTopic() {
        return KafkaConnection.createConsumer(dataset.getDatastoreProperties()).listTopics().keySet();
    }

    /**
     * Schema be infer by format and first record receive by kafka topic. CSV format, use field delimiter to split the
     * record, and assume all field type are string. Avro format, can not infer schema by this format, let the user
     * provide the schema.
     * <ol>
     * <li>Option 1, user edit schema, add column and type</li>
     * <li>Option 2, user configure avro schema string in {@link KafkaDatasetProperties#avroSchema}, then this method
     * will fill schema by this string</li>
     * </ol>
     * 
     * @return
     */
    @Override
    public Schema getSchema() {
        switch (dataset.valueFormat.getValue()) {
        case CSV: {
            // Simple schema container.
            final Schema[] s = new Schema[1];
            s[0] = AvroUtils.createEmptySchema();
            // Try to get one record and determine its schema in a callback.
            getSample(1, new Consumer<IndexedRecord>() {

                @Override
                public void accept(IndexedRecord in) {
                    s[0] = in.getSchema();
                }
            });
            // Return the discovered schema.
            return s[0];
        }
        case AVRO: {
            if (!dataset.isHierarchy.getValue()) { // no avro schema provided, let user edit schema directly
                // nothing to do, keep original
            } else { // use {@link KafkaDatasetProperties#avroSchema} generate schema
                String avroSchemaStr = dataset.avroSchema.getValue();
                if (avroSchemaStr != null && !"".equals(avroSchemaStr)) {
                    return new Schema.Parser().parse(avroSchemaStr);
                }
            }
            break;
        }
        default:
            break;
        }
        return dataset.main.schema.getValue();

    }

    /**
     * @param limit the maximum number of records to return.
     * @param consumer a callback that will be applied to each sampled record. This callback should throw a
     * {@link org.talend.daikon.exception.TalendRuntimeException} if there was an error processing the record. Kafka is
     * a unbounded source, have to set time out to stop reading, 1 second as the time out for get Sample, no matter if
     * it get sample or not.
     */
    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        // Create an input runtime based on the properties.
        KafkaInputPTransformRuntime inputRuntime = new KafkaInputPTransformRuntime();
        KafkaInputProperties inputProperties = new KafkaInputProperties(null);
        inputProperties.init();
        inputProperties.setDatasetProperties(dataset);
        inputProperties.useMaxReadTime.setValue(true);
        inputProperties.maxReadTime.setValue(1000l);
        inputProperties.autoOffsetReset.setValue(KafkaInputProperties.OffsetType.EARLIEST);
        inputRuntime.initialize(null, inputProperties);

        // Create a pipeline using the input component to get records.
        PipelineOptions options = PipelineOptionsFactory.create();
        final Pipeline p = Pipeline.create(options);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(inputRuntime) //
                    .apply(Sample.<IndexedRecord> any(limit)).apply(collector);
            p.run();
        }
    }
}
