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
package org.talend.components.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.talend.components.test.RecordSetUtil.getSimpleTestData;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.hdfs.AvroHDFSFileSource;
import org.apache.beam.sdk.io.hdfs.HDFSFileSource;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Keys;
import org.apache.beam.sdk.transforms.Sample;
import org.apache.beam.sdk.transforms.Values;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSTestUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.daikon.java8.Consumer;

/**
 * Unit test for {@link MiniDfsResource}.
 */
public class MiniDfsResourceTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    /**
     * An example of using the MiniDFSCluster.
     */
    @Test
    public void testBasic() throws IOException, URISyntaxException {
        mini.writeFile(mini.getFs(), "/user/test/stuff.txt", "1;one", "2;two", "3;three");

        assertThat(mini.getFs().exists(new Path("/user/test/stuff.txt")), is(true));
        FileStatus[] status = mini.getFs().listStatus(new Path("/user/test/"));
        assertThat(status, arrayWithSize(1));

        // Read the file in one chunk.
        assertThat(DFSTestUtil.readFile(mini.getFs(), status[0].getPath()), is("1;one\n2;two\n3;three\n"));

        // Read the file as lines.
        mini.assertReadFile(mini.getFs(), "/user/test/stuff.txt", "1;one", "2;two", "3;three");
    }

    /**
     * An example of using the MiniDFSCluster in Beam.
     */
    @Test
    public void testBasicBeam() throws IOException, URISyntaxException {
        mini.writeFile(mini.getFs(), "/user/test/stuff.txt", "1;one", "2;two", "3;three");

        // Create an input runtime based on the properties.
        String fileSpec = mini.getFs().getUri().resolve("/user/test/").toString();
        HDFSFileSource<LongWritable, Text> source = HDFSFileSource.from(fileSpec, TextInputFormat.class, LongWritable.class,
                Text.class);

        // Create a pipeline using the input component to get records.
        Pipeline p = TestPipeline.create();

        Consumer<Text> consumer = new Consumer<Text>() {

            @Override
            public void accept(Text s) {
                assertThat(s.toString(), anyOf(is("1;one"), is("2;two"), is("3;three")));
            }
        };

        try (DirectConsumerCollector<Text> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(Read.from(source)) //
                    .apply(Values.<Text> create()) //
                    .apply(Sample.<Text> any(100)) //
                    .apply(collector);
            p.run().waitUntilFinish();
        }
    }

    @Test
    public void testBasicBeamAvro() throws IOException, URISyntaxException {
        RecordSet rs = getSimpleTestData(0);
        RecordSetUtil.writeRandomAvroFile(mini.getFs(), "/user/test/input.avro", rs);
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.avro").toString();

        // Create an input runtime based on the properties.
        AvroHDFSFileSource<IndexedRecord> source = new AvroHDFSFileSource(fileSpec, AvroCoder.of(rs.getSchema()));

        // Create a pipeline using the input component to get records.
        Pipeline p = TestPipeline.create();

        final Integer[] count = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        Consumer<AvroKey<IndexedRecord>> consumer = new Consumer<AvroKey<IndexedRecord>>() {

            @Override
            public void accept(AvroKey<IndexedRecord> s) {
                count[(int) s.datum().get(0)]++;
            }
        };

        try (DirectConsumerCollector collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(Read.from(source)) //
                    .apply(Keys.<AvroKey<IndexedRecord>> create()) //
                    .apply(collector);
            p.run().waitUntilFinish();
        }

        // Assert that each row was counted only once.
        assertThat(count, arrayContaining(1, 1, 1, 1, 1, 1, 1, 1, 1, 1));

    }
}
