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
package org.talend.components.adapter.beam.io.rowgenerator;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.RunnableOnService;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.talend.components.adapter.beam.io.rowgenerator.RowGeneratorIO.BoundedRowGeneratorSource;
import org.talend.daikon.avro.SampleSchemas;

/**
 * Unit tests for {@link RowGeneratorIO}.
 */
@RunWith(JUnit4.class)
public class RowGeneratorIOTest {

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    @Test
    @Category(RunnableOnService.class)
    public void testBasic() throws Exception {
        // Non-deterministic read, just do a count.
        PCollection<IndexedRecord> output = pipeline.apply(RowGeneratorIO.read().withSchema(SampleSchemas.recordSimple()));
        PAssert.thatSingleton(output.apply("Count", Count.<IndexedRecord> globally())).isEqualTo(100L);
        pipeline.run();
    }

    /** Check that the rows are correctly distributed among partitions. */
    @Test
    public void testSourcePartitioning() throws Exception {
        PipelineOptions options = PipelineOptionsFactory.create();
        RowGeneratorIO.Read read = RowGeneratorIO.read().withSchema(SampleSchemas.recordSimple()).withRows(95).withPartitions(10);

        // The initial source generated is not yet partitioned.
        BoundedRowGeneratorSource initialSource = new BoundedRowGeneratorSource(read);
        assertThat("Wrong unsplit partition id", initialSource.getPartitionId(), is(-1));
        assertThat("Wrong unsplit row id", initialSource.getStartRowId(), is(0L));
        assertThat("Wrong unsplit number of rows", initialSource.getNumRows(), is(95L));

        // Ensure that the rows are evenly distributed across partitions after split.
        List<BoundedRowGeneratorSource> sources = initialSource.splitIntoBundles(0L, options);
        assertThat("All partitions generated", sources, hasSize(10));

        // The calculated expected partitions and row IDs are incrementally generated and in order.
        long expectedRowId = 0L;
        for (int partitionId = 0; partitionId < sources.size(); partitionId++) {
            BoundedRowGeneratorSource src = sources.get(partitionId);
            assertThat("Partition size", src.getNumRows(), either(is(9L)).or(is(10L)));
            assertThat("Can not be resplit", src.splitIntoBundles(0L, options), contains(src));
            assertThat("Wrong partition id", src.getPartitionId(), is(partitionId));
            for (int i = 0; i < src.getNumRows(); i++) {
                long rowId = src.getStartRowId() + i;
                assertThat("Wrong row id", rowId, is(expectedRowId));
                expectedRowId++;
            }
        }
        assertThat("All rows generated", expectedRowId, is(95L));
    }

    @Test
    @Category(RunnableOnService.class)
    public void testBasicDeterministic() throws Exception {
        // Non-deterministic read, just do a count.
        PCollection<IndexedRecord> output = pipeline.apply(RowGeneratorIO.read()
                .withSchema(SampleSchemas.recordCompositesRequired()).withSeed(0L).withRows(95L).withPartitions(13));
        PAssert.thatSingleton(output.apply("Count", Count.<IndexedRecord> globally())).isEqualTo(95L);
        pipeline.run();

        // TODO: we could test the generated records here.
    }

}
