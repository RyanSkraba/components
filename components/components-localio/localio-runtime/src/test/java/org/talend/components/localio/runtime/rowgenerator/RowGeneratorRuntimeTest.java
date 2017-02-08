package org.talend.components.localio.runtime.rowgenerator;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.localio.rowgenerator.RowGeneratorProperties;
import org.talend.daikon.avro.SampleSchemas;

/**
 * Unit tests for {@link RowGeneratorRuntime}.
 */
public class RowGeneratorRuntimeTest {

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testBasic() {
        RowGeneratorProperties componentProps = new RowGeneratorProperties(null);
        componentProps.init();
        componentProps.schemaFlow.schema.setValue(SampleSchemas.recordSimple());
        componentProps.nbRows.setValue(95L);
        componentProps.nbPartitions.setValue(10);
        RowGeneratorRuntime runtime = new RowGeneratorRuntime();
        runtime.initialize(null, componentProps);

        // Use the component in the pipeline.
        PCollection<IndexedRecord> output = pipeline.apply(runtime);
        PAssert.thatSingleton(output.apply("Count", Count.<IndexedRecord> globally())).isEqualTo(95L);
        pipeline.run().waitUntilFinish();
    }
}