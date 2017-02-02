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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.adapter.beam.io.rowgenerator.GeneratorFunction.GeneratorContext;
import org.talend.daikon.avro.SampleSchemas;

/**
 * Unit tests for {@link GeneratorFunction}.
 *
 * When comparing Avro records, it's very convenient and readable to compare the String representation versus a deep
 * comparison of record data.
 */
public class GeneratorFunctionsTest {

    /**
     * Factory method to create a {@link GeneratorContext} for testing, with rowId and pseudorandom number generator
     * preset.
     *
     * @param partitionId The partitionId to set for this context.
     * @param rowId The rowId to set for this context, also used as the random seed.
     * @return A reusable instance to use in creating rows.
     */
    public static GeneratorContext generatorContextOf(int partitionId, long rowId) {
        GeneratorContext ctx = GeneratorContext.of(partitionId);
        ctx.setRowId(rowId);
        ctx.setRandom(new Random(rowId));
        return ctx;
    }

    /**
     * Utility method to generate a function for the given schema and check that it is appropriately deterministic.
     */
    public static GeneratorFunction<IndexedRecord> generatorOfRecord(Schema schema) {
        // Create a function generator and context.
        GeneratorFunction<IndexedRecord> fn = GeneratorFunctions.ofRecord(schema);
        assertThat(fn, notNullValue());

        // Create a record.
        GeneratorContext ctx = generatorContextOf(0, 0L);
        IndexedRecord r1 = fn.apply(ctx);

        // Verify that reusing the function generator with the same context returns the same record.
        ctx.setRandom(new Random(0));
        ctx.setRowId(0);
        assertThat(fn.apply(ctx).toString(), equalTo(r1.toString()));

        // Verify that generating a new function generator with the same schema returns the same record.
        assertThat(GeneratorFunctions.ofRecord(schema).apply(generatorContextOf(0, 0L)).toString(), equalTo(r1.toString()));

        // Return the generator for further testing.
        return fn;
    }

    @Test
    public void testBasic() {
        // A very basic generator.
        GeneratorFunction<IndexedRecord> fn = generatorOfRecord(SampleSchemas.recordSimple());

        // Create a record.
        GeneratorContext ctx = generatorContextOf(0, 0L);
        IndexedRecord r1 = fn.apply(ctx);
        assertThat(r1.getSchema(), is(SampleSchemas.recordSimple()));
        assertThat(r1.get(0), isA((Class) Integer.class));
        assertThat(r1.get(1), isA((Class) String.class));

        // Create a second record from the same context.
        ctx.setRowId(1);
        ctx.setRandom(new Random(1));
        IndexedRecord r2 = fn.apply(ctx);

        // The records should be different
        assertThat(r2.toString(), not(equalTo(r1.toString())));

        // Create a record from a different context with different values.
        IndexedRecord r3 = fn.apply(generatorContextOf(1, 1));
        assertThat(r3.toString(), not(equalTo(r1.toString())));
    }

    @Test
    public void testGeneratorContext() {
        GeneratorContext ctx = GeneratorContext.of(123);
        assertThat(ctx.getPartitionId(), is(123));
        assertThat(ctx.getRowId(), is(0L));
        assertThat(ctx.getRandom(), nullValue());

        Random random = new Random(0);
        ctx.setRandom(random);
        ctx.setRowId(234L);
        assertThat(ctx.getPartitionId(), is(123));
        assertThat(ctx.getRowId(), is(234L));
        assertThat(ctx.getRandom(), sameInstance(random));
    }

    @Test
    public void testRecordPrimitivesRequired() {
        // Check the types generated for these record types.
        GeneratorFunction<IndexedRecord> fn = generatorOfRecord(SampleSchemas.recordPrimitivesRequired());
        GeneratorContext ctx = generatorContextOf(0, 0L);
        for (int i = 0; i < 100; i++) {
            IndexedRecord r1 = fn.apply(ctx);
            for (int field = 0; field < SampleSchemas.recordPrimitivesClasses.length; field++) {
                assertThat(r1.get(field), isA(SampleSchemas.recordPrimitivesClasses[field]));
            }
        }
    }

    @Test
    public void testRecordPrimitivesNullable() {
        // Primitives are potentially null (by default, ~50% of the time).
        GeneratorFunction<IndexedRecord> fn = generatorOfRecord(SampleSchemas.recordPrimitivesNullable());
        GeneratorContext ctx = generatorContextOf(0, 0L);
        double countNulls = 0;
        for (int i = 0; i < 100; i++) {
            IndexedRecord r1 = fn.apply(ctx);
            for (int field = 0; field < SampleSchemas.recordPrimitivesClasses.length; field++) {
                if (r1.get(field) == null)
                    countNulls++;
                else
                    assertThat(r1.get(field), isA(SampleSchemas.recordPrimitivesClasses[field]));
            }
        }
        // Of the 700 generated fields ~50% were null.
        assertThat(countNulls / SampleSchemas.recordPrimitivesClasses.length, closeTo(50, 5));
    }

    @Test
    public void testRecordPrimitivesOptional() {
        // Primitives are potentially null (by default, ~50% of the time).
        GeneratorFunction<IndexedRecord> fn = generatorOfRecord(SampleSchemas.recordPrimitivesOptional());
        GeneratorContext ctx = generatorContextOf(0, 0L);
        double countNulls = 0;
        for (int i = 0; i < 100; i++) {
            IndexedRecord r1 = fn.apply(ctx);
            for (int field = 0; field < SampleSchemas.recordPrimitivesClasses.length; field++) {
                if (r1.get(field) == null)
                    countNulls++;
                else
                    assertThat(r1.get(field), isA(SampleSchemas.recordPrimitivesClasses[field]));
            }
        }
        // Of the 700 generated fields ~50% were null.
        assertThat(countNulls / SampleSchemas.recordPrimitivesClasses.length, closeTo(50, 5));
    }

    @Test
    public void testRecordCompositesRequired() {
        // Check the types generated for these record types.
        GeneratorFunction<IndexedRecord> fn = generatorOfRecord(SampleSchemas.recordCompositesRequired());
        GeneratorContext ctx = generatorContextOf(0, 0L);
        for (int i = 0; i < 100; i++) {
            IndexedRecord r1 = fn.apply(ctx);
            for (int field = 0; field < SampleSchemas.recordCompositesClasses.length; field++) {
                assertThat(r1.get(field), isA(SampleSchemas.recordCompositesClasses[field]));
            }
        }
    }

}