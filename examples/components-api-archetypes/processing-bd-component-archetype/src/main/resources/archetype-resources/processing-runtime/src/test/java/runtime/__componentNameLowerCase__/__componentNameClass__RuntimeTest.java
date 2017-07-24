package ${package}.runtime.${componentNameLowerCase};

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
import org.talend.components.processing.definition.${componentNameLowerCase}.${componentNameClass}Properties;
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

public class ${componentNameClass}RuntimeTest {

    private final ${componentNameClass}Runtime ${componentNameLowerCase}Runtime = new ${componentNameClass}Runtime();

    /**
     * Check {@link ${componentNameClass}Runtime#initialize(RuntimeContainer, Properties)}
     */
    @Test
    public void testInitialize() {
        ValidationResult result = ${componentNameLowerCase}Runtime.initialize(null, null);
        assertEquals(ValidationResult.OK, result);
    }

    /**
     * Check {@link ${componentNameClass}Runtime#build(BeamJobContext)}
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
        /*
        *   Example of test
        *
        List<IndexedRecord> data = Arrays.asList( //
                irA, //
                irB, //
                irC, //
                irA, //
                irA, //
                irC //
        );

        PCollection<IndexedRecord> input = (PCollection<IndexedRecord>) p.apply(Create.of(data).withCoder(LazyAvroCoder.of()));

        ${componentNameClass}Properties ${componentNameLowerCase}Properties = new ${componentNameClass}Properties("test");
        ${componentNameLowerCase}Runtime.initialize(null, ${componentNameLowerCase}Properties);
        BeamJobContext context = Mockito.mock(BeamJobContext.class);
        ${componentNameLowerCase}Runtime.build(context);
        verify(context, times(1)).getLinkNameByPortName(anyString());
        verify(context, times(0)).getPCollectionByLinkName(anyString());

        BeamJobContext ctx = Mockito.mock(BeamJobContext.class);
        when(ctx.getLinkNameByPortName(anyString())).thenReturn("test");
        when(ctx.getPCollectionByLinkName(anyString())).thenReturn(input);
        ${componentNameLowerCase}Runtime.build(ctx);
        verify(ctx, times(2)).getLinkNameByPortName(anyString());
        verify(ctx, times(1)).getPCollectionByLinkName(anyString());
         */
    }
}
