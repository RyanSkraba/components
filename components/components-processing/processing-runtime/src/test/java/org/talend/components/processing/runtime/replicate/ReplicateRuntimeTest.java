package org.talend.components.processing.runtime.replicate;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.adapter.beam.BeamJobContext;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.replicate.ReplicateProperties;
import org.talend.components.processing.runtime.replicate.ReplicateRuntime;
import org.talend.daikon.avro.GenericDataRecordHelper;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReplicateRuntimeTest {

    private final ReplicateRuntime replicateRuntime = new ReplicateRuntime();

    /**
     * Check {@link ReplicateRuntime#initialize(RuntimeContainer, Properties)}
     */
    @Test
    public void testInitialize() {
        ValidationResult result = replicateRuntime.initialize(null, null);
        assertEquals(ValidationResult.OK, result);
    }

    /**
     * Check {@link ReplicateRuntime#build(BeamJobContext)}
     */
    @Test
    public void testBuild() {

        // Create pipeline
        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(options);

        // Create PCollection for test
        Schema a = GenericDataRecordHelper.createSchemaFromObject("a", new Object[] { "a" });
        IndexedRecord irA = GenericDataRecordHelper.createRecord(a, new Object[] { "a" });
        IndexedRecord irB = GenericDataRecordHelper.createRecord(a, new Object[] { "b" });
        IndexedRecord irC = GenericDataRecordHelper.createRecord(a, new Object[] { "c" });

        List<IndexedRecord> data = Arrays.asList( //
                irA, //
                irB, //
                irC, //
                irA, //
                irA, //
                irC //
        );

        PCollection<IndexedRecord> input = (PCollection<IndexedRecord>) p.apply(Create.of(data).withCoder(LazyAvroCoder.of()));

        ReplicateProperties replicateProperties = new ReplicateProperties("test");
        replicateRuntime.initialize(null, replicateProperties);
        BeamJobContext context = Mockito.mock(BeamJobContext.class);
        replicateRuntime.build(context);
        verify(context, times(1)).getLinkNameByPortName(anyString());
        verify(context, times(0)).getPCollectionByLinkName(anyString());

        BeamJobContext ctx = Mockito.mock(BeamJobContext.class);
        when(ctx.getLinkNameByPortName(anyString())).thenReturn("test");
        when(ctx.getPCollectionByLinkName(anyString())).thenReturn(input);
        replicateRuntime.build(ctx);
        verify(ctx, times(3)).getLinkNameByPortName(anyString());
        verify(ctx, times(1)).getPCollectionByLinkName(anyString());
    }
}
