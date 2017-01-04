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

package ${package}.runtime${runtimeVersionConverted};

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
//import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Sample;
import ${packageTalend}.adapter.beam.coders.LazyAvroCoder;
import ${packageTalend}.adapter.beam.transform.DirectConsumerCollector;
import ${packageTalend}.api.container.RuntimeContainer;
import ${packageTalend}.common.dataset.runtime.DatasetRuntime;
import ${packageTalend}.${componentNameLowerCase}.${componentNameClass}DatasetProperties;
import ${packageTalend}.${componentNameLowerCase}.input.${componentNameClass}InputProperties;
import ${packageDaikon}.java8.Consumer;
import ${packageDaikon}.properties.ValidationResult;

public class ${componentNameClass}DatasetRuntime implements DatasetRuntime<${componentNameClass}DatasetProperties> {

    /**
     * The dataset instance that this runtime is configured for.
     */
    private ${componentNameClass}DatasetProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ${componentNameClass}DatasetProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        // Simple schema container.
        final Schema[] s = new Schema[1];
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

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        // Create an input runtime based on the properties.
        ${componentNameClass}InputRuntime inputRuntime = new ${componentNameClass}InputRuntime();
        ${componentNameClass}InputProperties inputProperties = new ${componentNameClass}InputProperties(null);
        inputProperties.init();
        inputProperties.setDatasetProperties(properties);
        inputRuntime.initialize(null, inputProperties);

        // Create a pipeline using the input component to get records.
        PipelineOptions options = PipelineOptionsFactory.create();
//        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);
        LazyAvroCoder.registerAsFallback(p);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(inputRuntime) //
                    .apply(Sample.<IndexedRecord> any(limit)).apply(collector);
            p.run();
        }
    }
}
