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
package org.talend.components.adapter.beam.transform;

import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Test;
import org.talend.daikon.avro.converter.SingleColumnIndexedRecordConverter;

/**
 * Unit tests for {@link ConvertToIndexedRecord}.
 */
public class ConvertToIndexedRecordTest {

    /**
     * Demonstrates the basic use case for the {@link ConvertToIndexedRecord}.
     */
    @Test
    public void testBasic() {

        String[] inputValues = { "one", "two", "three" };
        // The output values should use the standard primitive converter.
        SingleColumnIndexedRecordConverter<String> converter = new SingleColumnIndexedRecordConverter(String.class,
                Schema.create(Schema.Type.STRING));
        IndexedRecord[] outputExpected = new IndexedRecord[inputValues.length];
        for (int i = 0; i < inputValues.length; i++)
            outputExpected[i] = converter.convertToAvro(inputValues[i]);

        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

        PCollection<String> input = p.apply(Create.of(Arrays.asList(inputValues))); //

        // Collect the results before and after the transformation.
        PCollection<IndexedRecord> output = input.apply(ConvertToIndexedRecord.<String> of());

        // Validate the contents of the collections in the pipeline.
        PAssert.that(input).containsInAnyOrder(inputValues);
        PAssert.that(output).containsInAnyOrder(outputExpected);

        // Run the pipeline to fill the collectors.
        p.run().waitUntilFinish();
    }

}
