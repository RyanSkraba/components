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
package org.talend.components.simplefileio.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Sample;
import org.talend.components.adapter.beam.BeamLocalRunnerOption;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.SimpleFileIODatastoreProperties;
import org.talend.components.simplefileio.input.SimpleFileIOInputProperties;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.runtime.ugi.UgiExceptionHandler;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

public class SimpleFileIODatasetRuntime implements DatasetRuntime<SimpleFileIODatasetProperties> {

    /**
     * The dataset instance that this runtime is configured for.
     */
    private SimpleFileIODatasetProperties properties = null;

    /**
     * Helper method for any runtime to get the appropriate {@link UgiDoAs} for executing.
     *
     * @param datasetProperties dataset properties, containing credentials for the cluster.
     * @param accessType the type of access to the dataset that the user will be performing.
     * @return An object that can be used to execute actions with the correct credentials.
     */
    public static UgiDoAs getReadWriteUgiDoAs(SimpleFileIODatasetProperties datasetProperties,
            UgiExceptionHandler.AccessType accessType) {
        String path = datasetProperties.path.getValue();
        SimpleFileIODatastoreProperties datastoreProperties = datasetProperties.getDatastoreProperties();
        if (datastoreProperties.useKerberos.getValue()) {
            UgiDoAs doAs = UgiDoAs.ofKerberos(datastoreProperties.kerberosPrincipal.getValue(),
                    datastoreProperties.kerberosKeytab.getValue());
            return new UgiExceptionHandler(doAs, accessType, datastoreProperties.kerberosPrincipal.getValue(), path);
        } else if (datastoreProperties.userName.getValue() != null && !datastoreProperties.userName.getValue().isEmpty()) {
            UgiDoAs doAs = UgiDoAs.ofSimple(datastoreProperties.userName.getValue());
            return new UgiExceptionHandler(doAs, accessType, datastoreProperties.userName.getValue(), path);
        } else {
            return new UgiExceptionHandler(UgiDoAs.ofNone(), accessType, null, path);
        }
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIODatasetProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        // Simple schema container.
        final Schema[] s = new Schema[] { AvroUtils.createEmptySchema() };
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

    //getSample is not a good name for the data set interface, as sometimes, it is used to fetch all data in data set definition, not sample
    //Or if it's a good name and only for get sample, for the rest api to get all data in data set should not call this method, should call another interface for
    //fetch all data
    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        // Create an input runtime based on the properties.
        SimpleFileIOInputRuntime inputRuntime = new SimpleFileIOInputRuntime();
        SimpleFileIOInputProperties inputProperties = new SimpleFileIOInputProperties(null);
        inputProperties.limit.setValue(limit);
        inputProperties.init();
        inputProperties.setDatasetProperties(properties);
        inputRuntime.initialize(null, inputProperties);
        // Create a pipeline using the input component to get records.

        DirectOptions options = BeamLocalRunnerOption.getOptions();
        final Pipeline p = Pipeline.create(options);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(inputRuntime) //
                    .apply(collector);
            try {
                p.run().waitUntilFinish();
            } catch (Pipeline.PipelineExecutionException e) {
                if (e.getCause() instanceof TalendRuntimeException)
                    throw (TalendRuntimeException) e.getCause();
                throw e;
            }
        }
    }
}
