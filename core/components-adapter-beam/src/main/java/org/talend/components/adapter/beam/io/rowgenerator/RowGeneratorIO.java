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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;

/**
 * Transforms for reading generated Avro rows.
 *
 * <h3>Generating Avro rows</h3>
 *
 * <p>
 * {@link RowGeneratorIO#read RowGeneratorIO.read()} returns a bounded {@link PCollection
 * PCollection&lt;IndexedRecord&gt;} that match the specified schema.
 *
 * <pre>
 * {@code
 * pipeline.apply(RowGeneratorIO.read().withSchema(mySchema)
 * }
 * </pre>
 *
 * TODO(rskraba): describe the types of data used to fill the indexed record.
 */
public class RowGeneratorIO {

    public static Read read() {
        return new AutoValue_RowGeneratorIO_Read.Builder().setRows(100L).setPartitions(10).build();
    }

    private RowGeneratorIO() {
    }

    /** A {@link PTransform} reading generated data. */
    @AutoValue
    public abstract static class Read extends PTransform<PBegin, PCollection<IndexedRecord>> {

        /** Serialized as a String in jsonSchema. */
        private transient Schema schema = null;

        @Nullable
        abstract String getJsonSchema();

        /**
         * Get the seed used to generate "deterministic" random rows. This initializes Random instances so that their
         * pseudorandom sequences can be reproduced. If null, the generated results are not necessarily reproducable.
         * 
         * @return the seed used to generate "deterministic" random rows.
         */
        @Nullable
        abstract Long getSeed();

        abstract long getRows();

        abstract int getPartitions();

        abstract Builder builder();

        @AutoValue.Builder
        abstract static class Builder {

            abstract Builder setJsonSchema(String schema);

            Builder setSchema(Schema schema) {
                return setJsonSchema(schema.toString());
            }

            abstract Builder setSeed(Long seed);

            abstract Builder setRows(long rows);

            abstract Builder setPartitions(int partitions);

            abstract Read build();
        }

        /**
         * Provide the schema for the generated rows.
         *
         * @param schema the Schema to use in the generated rows.
         * @return the {@link Read} with the specified schema
         */
        public Read withSchema(Schema schema) {
            checkArgument(schema != null, "RowGeneratorIO.read().withSchema(schema) called with null schema");
            checkArgument(schema.getType() == Schema.Type.RECORD,
                    "RowGeneratorIO.read().withSchema(schema) called with a non-record schema: %s", schema.getType().toString());
            this.schema = schema;
            return builder().setSchema(schema).build();
        }

        /**
         * @return the schema used to generate rows.
         */
        public Schema getSchema() {
            // This works around non-serializable Avro Schema.
            if (schema == null) {
                schema = new Schema.Parser().parse(getJsonSchema());
            }
            return schema;
        }

        /**
         * Provide a seed to reproduce generated rows. A {@link RowGeneratorIO.Read} with the same seed and schema
         * should produce deterministic rows between runs.
         *
         * @param seed A seed to use when generating random values, or null to select the current time.
         * @return the {@link Read} with seed set.
         */
        public Read withSeed(Long seed) {
            return builder().setSeed(seed == null ? System.currentTimeMillis() : seed).build();
        }

        /**
         * Provide the expected total number of rows to be generated.
         *
         * @param rows number of rows to generate.
         * @return the {@link Read} with number of rows set.
         */
        public Read withRows(long rows) {
            checkArgument(rows > 0, "RowGeneratorIO.read().withRows(rows) called with a negative " + "or equal to 0 value: %s",
                    rows);
            return builder().setRows(rows).build();
        }

        /**
         * Provide the expected initial number of partitions to be generated.
         *
         * @param partitions number of partitions to generate.
         * @return the {@link Read} with number of partitions set.
         */
        public Read withPartitions(int partitions) {
            checkArgument(partitions > 0, "RowGeneratorIO.read().withPartitions(partitions) called with a negative "
                    + "or equal to 0 value: %s", partitions);
            return builder().setPartitions(partitions).build();
        }

        @Override
        public PCollection<IndexedRecord> expand(PBegin input) {
            return input.apply(org.apache.beam.sdk.io.Read.from(new BoundedRowGeneratorSource(this)));
        }

        @Override
        public void validate(PBegin input) {
            checkState(getJsonSchema() != null, "RowGeneratorIO.read() requires a schema" + " to be set via withSchema(schema)");
            checkState(getRows() >= 0, "RowGeneratorIO.read() requires withRows(rows)"
                    + " to be called with a non-negative value.");
            checkState(getPartitions() >= 0, "RowGeneratorIO.read() requires withPartitions(partitions)"
                    + " to be called with a non-negative value.");
        }

        @Override
        public void populateDisplayData(DisplayData.Builder builder) {
            super.populateDisplayData(builder);
            builder.add(DisplayData.item("jsonSchema", getJsonSchema()));
            builder.addIfNotNull(DisplayData.item("seed", getSeed()));
            builder.add(DisplayData.item("rows", getRows()));
            builder.add(DisplayData.item("partitions", getPartitions()));
        }
    }

    /** A {@link BoundedSource} reading from Elasticsearch. */
    @VisibleForTesting
    static class BoundedRowGeneratorSource extends BoundedSource<IndexedRecord> {

        private final RowGeneratorIO.Read spec;

        /** The partition ID, numbered from 0, or -1 if the source hasn't been split yet. */
        private final int partitionId;

        /** The starting ID for the rows that this source generates. */
        private final long startRowId;

        /** The number rows that this source generates. */
        private final long numRows;

        private GeneratorFunction<IndexedRecord> generator;

        private transient Schema schema;

        @VisibleForTesting
        BoundedRowGeneratorSource(Read spec) {
            this(spec, -1, 0L, spec.getRows());
        }

        @VisibleForTesting
        BoundedRowGeneratorSource(Read spec, int partitionId, long startRow, long numRows) {
            this.spec = spec;
            this.partitionId = partitionId;
            this.startRowId = startRow;
            this.numRows = numRows;
            this.generator = GeneratorFunctions.ofRecord(spec.getSchema());
        }

        @VisibleForTesting
        int getPartitionId() {
            return partitionId;
        }

        @VisibleForTesting
        long getStartRowId() {
            return startRowId;
        }

        @VisibleForTesting
        long getNumRows() {
            return numRows;
        }

        @Override
        public List<BoundedRowGeneratorSource> splitIntoBundles(long desiredBundleSizeBytes, PipelineOptions options)
                throws Exception {
            // The desiredBundleSizeBytes is ignored to split the rows into the bundles specified by the spec.
            if (partitionId != -1) {
                // If it has already been split, it can't be split further.
                return Arrays.asList(this);
            }

            List<BoundedRowGeneratorSource> bundles = new ArrayList<>(spec.getPartitions());

            // Evenly divide the rows over all the bundles but the last.
            double rowsPerBundle = (double) spec.getRows() / spec.getPartitions();
            long startRowId = 0;
            for (int partition = 0; partition < spec.getPartitions() - 1; partition++) {
                long newRows = (long) (rowsPerBundle * (partition + 1)) - startRowId;
                bundles.add(new BoundedRowGeneratorSource(spec, partition, startRowId, newRows));
                startRowId += newRows;
            }

            // Put all of the remaining rows in the last bundle.
            bundles.add(new BoundedRowGeneratorSource(spec, spec.getPartitions() - 1, startRowId, spec.getRows() - startRowId));
            return bundles;
        }

        @Override
        public long getEstimatedSizeBytes(PipelineOptions options) throws IOException {
            return -1;
        }

        @Override
        public void populateDisplayData(DisplayData.Builder builder) {
            spec.populateDisplayData(builder);
        }

        @Override
        public BoundedReader<IndexedRecord> createReader(PipelineOptions options) throws IOException {
            return new BoundedRowGeneratorReader(this);
        }

        @Override
        public void validate() {
            spec.validate(null);
        }

        @Override
        public Coder<IndexedRecord> getDefaultOutputCoder() {
            return AvroCoder.of(IndexedRecord.class, spec.getSchema());
        }
    }

    private static class BoundedRowGeneratorReader extends BoundedSource.BoundedReader<IndexedRecord> {

        private final BoundedRowGeneratorSource source;

        private IndexedRecord current = null;

        private long count = 0;

        private final GeneratorFunction.GeneratorContext ctx;

        private BoundedRowGeneratorReader(BoundedRowGeneratorSource source) {
            this.source = source;
            this.ctx = GeneratorFunction.GeneratorContext.of(source.partitionId);
        }

        @Override
        public boolean start() {
            // Set the random seed once if it hasn't already been set.
            if (source.spec.getSeed() == null) {
                ctx.setRandom(new Random(System.currentTimeMillis() + source.partitionId));
            }
            return advance();
        }

        @Override
        public boolean advance() {
            // Stopping condition.
            if (count >= source.numRows) {
                current = null;
                return false;
            }

            // Update the row generator context for the next row.
            ctx.setRowId(source.startRowId + count);
            if (source.spec.getSeed() != null) {
                ctx.setRandom(new Random(source.spec.getSeed() + ctx.getRowId()));
            }

            current = source.generator.apply(ctx);
            count++;
            return true;
        }

        @Override
        public IndexedRecord getCurrent() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current;
        }

        @Override
        public void close() {
        }

        @Override
        public BoundedRowGeneratorSource getCurrentSource() {
            return source;
        }
    }
}
