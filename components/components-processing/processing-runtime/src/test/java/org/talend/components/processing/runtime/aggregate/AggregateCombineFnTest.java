package org.talend.components.processing.runtime.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.junit.Assert;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;

public class AggregateCombineFnTest {

    public static List<GenericData.Record> genRecords(List values, Schema fieldType) {
        Object defaultValue = null;
        Schema.Field f = new Schema.Field("v", AvroUtils.wrapAsNullable(fieldType), "", defaultValue);
        Schema fieldRecordSchema = Schema.createRecord(Arrays.asList(new Schema.Field[] { f }));
        List records = new ArrayList();
        for (Object value : values) {
            GenericData.Record fieldRecord = new GenericData.Record(fieldRecordSchema);
            fieldRecord.put(0, value);
            records.add(fieldRecord);
        }
        return records;
    }

    @Test
    public void AvgAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1, 2, 3, 4, 5), AvroUtils._int());
        AggregateCombineFn.AvgAccumulatorFn fn1 = new AggregateCombineFn.AvgAccumulatorFn();
        AggregateCombineFn.AvgAccumulatorFn fn2 = new AggregateCombineFn.AvgAccumulatorFn();
        AggregateCombineFn.AvgAccumulatorFn fn3 = new AggregateCombineFn.AvgAccumulatorFn();
        fn1.createAccumulator();
        fn2.createAccumulator();
        fn3.createAccumulator();
        double delta = 0;

        fn1.addInput(testData.get(0));
        Assert.assertEquals(1, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(1.5, fn1.extractOutput(), delta);

        fn2.addInput(testData.get(2));
        Assert.assertEquals(3, fn2.extractOutput(), delta);
        fn2.addInput(testData.get(3));
        Assert.assertEquals(3.5, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(3, fn1.extractOutput(), delta);
    }

    @Test
    public void SumDoubleAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1.1f, 2.2f, 3.3f, 4.4f, 5.5f), AvroUtils._float());
        AggregateCombineFn.SumDoubleAccumulatorFn fn1 = new AggregateCombineFn.SumDoubleAccumulatorFn();
        AggregateCombineFn.SumDoubleAccumulatorFn fn2 = new AggregateCombineFn.SumDoubleAccumulatorFn();
        AggregateCombineFn.SumDoubleAccumulatorFn fn3 = new AggregateCombineFn.SumDoubleAccumulatorFn();
        fn1.createAccumulator();
        fn2.createAccumulator();
        fn3.createAccumulator();
        double delta = 0.000000000000001;

        fn1.addInput(testData.get(0));
        Assert.assertEquals(1.1, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(3.3, fn1.extractOutput(), delta);

        fn2.addInput(testData.get(2));
        Assert.assertEquals(3.3, fn2.extractOutput(), delta);
        fn2.addInput(testData.get(3));
        Assert.assertEquals(7.7, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5.5, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(16.5, fn1.extractOutput(), delta);
    }

    @Test
    public void SumLongAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1, 2, 3, 4, 5), AvroUtils._int());
        AggregateCombineFn.SumLongAccumulatorFn fn1 = new AggregateCombineFn.SumLongAccumulatorFn();
        AggregateCombineFn.SumLongAccumulatorFn fn2 = new AggregateCombineFn.SumLongAccumulatorFn();
        AggregateCombineFn.SumLongAccumulatorFn fn3 = new AggregateCombineFn.SumLongAccumulatorFn();
        fn1.createAccumulator();
        fn2.createAccumulator();
        fn3.createAccumulator();
        int delta = 0;

        fn1.addInput(testData.get(0));
        Assert.assertEquals(1, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(3, fn1.extractOutput(), delta);

        fn2.addInput(testData.get(2));
        Assert.assertEquals(3, fn2.extractOutput(), delta);
        fn2.addInput(testData.get(3));
        Assert.assertEquals(7, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(15, fn1.extractOutput(), delta);
    }

    @Test
    public void CountAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1, 2, 3, 4, 5), AvroUtils._int());
        AggregateCombineFn.CountAccumulatorFn fn1 = new AggregateCombineFn.CountAccumulatorFn();
        AggregateCombineFn.CountAccumulatorFn fn2 = new AggregateCombineFn.CountAccumulatorFn();
        AggregateCombineFn.CountAccumulatorFn fn3 = new AggregateCombineFn.CountAccumulatorFn();
        fn1.createAccumulator();
        fn2.createAccumulator();
        fn3.createAccumulator();
        int delta = 0;
        Assert.assertEquals(0, fn1.extractOutput(), delta);

        fn1.addInput(testData.get(0));
        Assert.assertEquals(1, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2, fn1.extractOutput(), delta);

        fn2.addInput(testData.get(2));
        Assert.assertEquals(1, fn2.extractOutput(), delta);
        fn2.addInput(testData.get(3));
        Assert.assertEquals(2, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(1, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(5, fn1.extractOutput(), delta);
    }

    @Test
    public void ListAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1, 2, 3, 4, 5), AvroUtils._int());
        AggregateCombineFn.ListAccumulatorFn fn1 = new AggregateCombineFn.ListAccumulatorFn();
        AggregateCombineFn.ListAccumulatorFn fn2 = new AggregateCombineFn.ListAccumulatorFn();
        AggregateCombineFn.ListAccumulatorFn fn3 = new AggregateCombineFn.ListAccumulatorFn();
        fn1.createAccumulator();
        fn2.createAccumulator();
        fn3.createAccumulator();
        Assert.assertEquals(0, fn1.getAccumulators().size());

        fn1.addInput(testData.get(0));
        Assert.assertEquals(1, fn1.extractOutput().size());
        Assert.assertEquals(testData.get(0), fn1.getAccumulators().get(0));
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2, fn1.extractOutput().size());
        Assert.assertEquals(testData.get(0), fn1.getAccumulators().get(0));
        Assert.assertEquals(testData.get(1), fn1.getAccumulators().get(1));

        fn2.addInput(testData.get(2));
        Assert.assertEquals(1, fn2.extractOutput().size());
        Assert.assertEquals(testData.get(2), fn2.getAccumulators().get(0));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(2, fn2.extractOutput().size());
        Assert.assertEquals(testData.get(2), fn2.getAccumulators().get(0));
        Assert.assertEquals(testData.get(3), fn2.getAccumulators().get(1));

        fn3.addInput(testData.get(4));
        Assert.assertEquals(1, fn3.extractOutput().size());
        Assert.assertEquals(testData.get(4), fn3.getAccumulators().get(0));

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(5, fn1.extractOutput().size());
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), fn1.extractOutput());

    }

    @Test
    public void MinIntegerAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(3, 2, 10, 1, 5), AvroUtils._int());
        AggregateCombineFn.MinIntegerAccumulatorFn fn1 = new AggregateCombineFn.MinIntegerAccumulatorFn();
        AggregateCombineFn.MinIntegerAccumulatorFn fn2 = new AggregateCombineFn.MinIntegerAccumulatorFn();
        AggregateCombineFn.MinIntegerAccumulatorFn fn3 = new AggregateCombineFn.MinIntegerAccumulatorFn();
        fn1.createAccumulator();
        fn2.createAccumulator();
        fn3.createAccumulator();
        int delta = 0;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(3, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(1, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(1, fn1.extractOutput(), delta);
    }

    @Test
    public void MinLongAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(3l, 2l, 10l, 1l, 5l), AvroUtils._long());
        AggregateCombineFn.MinLongAccumulatorFn fn1 = new AggregateCombineFn.MinLongAccumulatorFn();
        AggregateCombineFn.MinLongAccumulatorFn fn2 = new AggregateCombineFn.MinLongAccumulatorFn();
        AggregateCombineFn.MinLongAccumulatorFn fn3 = new AggregateCombineFn.MinLongAccumulatorFn();
        fn1.createAccumulator();
        fn2.createAccumulator();
        fn3.createAccumulator();
        long delta = 0l;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(3l, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2l, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(1l, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5l, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(1l, fn1.extractOutput(), delta);
    }

    @Test
    public void MinFloatAccumulatorFnTest() {
        List<GenericData.Record> testData =
                genRecords(Arrays.asList(3.3f, 2.2f, 10.10f, 1.1f, 5.5f), AvroUtils._float());
        AggregateCombineFn.MinFloatAccumulatorFn fn1 = new AggregateCombineFn.MinFloatAccumulatorFn();
        fn1.createAccumulator();
        AggregateCombineFn.MinFloatAccumulatorFn fn2 = new AggregateCombineFn.MinFloatAccumulatorFn();
        fn2.createAccumulator();
        AggregateCombineFn.MinFloatAccumulatorFn fn3 = new AggregateCombineFn.MinFloatAccumulatorFn();
        fn3.createAccumulator();
        float delta = 0.0f;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(3.3f, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2.2f, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(1.1f, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5.5f, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(1.1f, fn1.extractOutput(), delta);
    }

    @Test
    public void MinDoubleAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(3.3, 2.2, 10.10, 1.1, 5.5), AvroUtils._double());

        AggregateCombineFn.MinDoubleAccumulatorFn fn1 = new AggregateCombineFn.MinDoubleAccumulatorFn();
        fn1.createAccumulator();
        AggregateCombineFn.MinDoubleAccumulatorFn fn2 = new AggregateCombineFn.MinDoubleAccumulatorFn();
        fn2.createAccumulator();
        AggregateCombineFn.MinDoubleAccumulatorFn fn3 = new AggregateCombineFn.MinDoubleAccumulatorFn();
        fn3.createAccumulator();
        double delta = 0.0;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(3.3, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2.2, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(1.1, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5.5, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(1.1, fn1.extractOutput(), delta);
    }

    @Test
    public void MaxIntegerAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1, 2, 3, 10, 5), AvroUtils._int());
        AggregateCombineFn.MaxIntegerAccumulatorFn fn1 = new AggregateCombineFn.MaxIntegerAccumulatorFn();
        fn1.createAccumulator();
        AggregateCombineFn.MaxIntegerAccumulatorFn fn2 = new AggregateCombineFn.MaxIntegerAccumulatorFn();
        fn2.createAccumulator();
        AggregateCombineFn.MaxIntegerAccumulatorFn fn3 = new AggregateCombineFn.MaxIntegerAccumulatorFn();
        fn3.createAccumulator();
        int delta = 0;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(1, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(10, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(10, fn1.extractOutput(), delta);
    }

    @Test
    public void MaxLongAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1l, 2l, 3l, 10l, 5l), AvroUtils._long());
        AggregateCombineFn.MaxLongAccumulatorFn fn1 = new AggregateCombineFn.MaxLongAccumulatorFn();
        fn1.createAccumulator();
        AggregateCombineFn.MaxLongAccumulatorFn fn2 = new AggregateCombineFn.MaxLongAccumulatorFn();
        fn2.createAccumulator();
        AggregateCombineFn.MaxLongAccumulatorFn fn3 = new AggregateCombineFn.MaxLongAccumulatorFn();
        fn3.createAccumulator();
        long delta = 0l;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(1l, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2l, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(10l, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5l, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(10l, fn1.extractOutput(), delta);
    }

    @Test
    public void MaxFloatAccumulatorFnTest() {
        List<GenericData.Record> testData =
                genRecords(Arrays.asList(1.1f, 2.2f, 3.3f, 10.10f, 5.5f), AvroUtils._float());
        AggregateCombineFn.MaxFloatAccumulatorFn fn1 = new AggregateCombineFn.MaxFloatAccumulatorFn();
        fn1.createAccumulator();
        AggregateCombineFn.MaxFloatAccumulatorFn fn2 = new AggregateCombineFn.MaxFloatAccumulatorFn();
        fn2.createAccumulator();
        AggregateCombineFn.MaxFloatAccumulatorFn fn3 = new AggregateCombineFn.MaxFloatAccumulatorFn();
        fn3.createAccumulator();
        float delta = 0.0f;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(1.1f, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2.2f, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(10.10f, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5.5f, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(10.10f, fn1.extractOutput(), delta);
    }

    @Test
    public void MaxDoubleAccumulatorFnTest() {
        List<GenericData.Record> testData = genRecords(Arrays.asList(1.1, 2.2, 3.3, 10.10, 5.5), AvroUtils._double());

        AggregateCombineFn.MaxDoubleAccumulatorFn fn1 = new AggregateCombineFn.MaxDoubleAccumulatorFn();
        fn1.createAccumulator();
        AggregateCombineFn.MaxDoubleAccumulatorFn fn2 = new AggregateCombineFn.MaxDoubleAccumulatorFn();
        fn2.createAccumulator();
        AggregateCombineFn.MaxDoubleAccumulatorFn fn3 = new AggregateCombineFn.MaxDoubleAccumulatorFn();
        fn3.createAccumulator();
        double delta = 0.0;
        fn1.addInput(testData.get(0));
        Assert.assertEquals(1.1, fn1.extractOutput(), delta);
        fn1.addInput(testData.get(1));
        Assert.assertEquals(2.2, fn1.getAccumulators(), delta);

        fn2.addInput(testData.get(2));
        fn2.addInput(testData.get(3));
        Assert.assertEquals(10.10, fn2.extractOutput(), delta);

        fn3.addInput(testData.get(4));
        Assert.assertEquals(5.5, fn3.extractOutput(), delta);

        fn1.mergeAccumulators(Arrays.asList(fn2.getAccumulators(), fn3.getAccumulators()));
        Assert.assertEquals(10.10, fn1.extractOutput(), delta);
    }
}
