package org.talend.components.processing.runtime.aggregate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.ListCoder;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Combine;
import org.talend.components.adapter.beam.kv.KeyValueUtils;
import org.talend.components.adapter.beam.kv.SchemaGeneratorUtils;
import org.talend.components.processing.definition.aggregate.AggregateFieldOperationType;
import org.talend.components.processing.definition.aggregate.AggregateOperationProperties;
import org.talend.components.processing.definition.aggregate.AggregateProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class AggregateCombineFn
        extends Combine.CombineFn<IndexedRecord, AggregateCombineFn.AggregateAccumulator, IndexedRecord> {

    private AggregateProperties properties;

    public AggregateCombineFn(AggregateProperties properties) {
        this.properties = properties;
    }

    @Override
    public AggregateAccumulator createAccumulator() {
        return new AggregateAccumulator(properties);
    }

    @Override
    public AggregateAccumulator addInput(AggregateAccumulator accumulator, IndexedRecord inputRecord) {
        if (accumulator.outputRecordSchemaStr == null) {
            accumulator.outputRecordSchemaStr =
                    AggregateUtils.genOutputRecordSchema(inputRecord.getSchema(), properties).toString();
        }
        for (AccumulatorElement accumulatorElement : accumulator.accumulatorElements) {
            accumulatorElement.addInput(inputRecord);
        }
        return accumulator;
    }

    @Override
    public AggregateAccumulator mergeAccumulators(Iterable<AggregateAccumulator> accumulators) {
        AggregateAccumulator deltaAcc = createAccumulator();
        for (int idx = 0; idx < properties.filteredOperations().size(); idx++) {
            List accs = new ArrayList();
            for (AggregateAccumulator accumulator : accumulators) {
                if (deltaAcc.outputRecordSchemaStr == null) {
                    deltaAcc.outputRecordSchemaStr = accumulator.outputRecordSchemaStr;
                }
                accs.add(accumulator.accumulatorElements.get(idx));
            }
            deltaAcc.accumulatorElements.get(idx).mergeAccumulators(accs);
        }
        return deltaAcc;
    }

    @Override
    public IndexedRecord extractOutput(AggregateAccumulator accumulator) {
        Schema.Parser parser = new Schema.Parser();
        Schema outputRecordSchema = parser.parse(accumulator.outputRecordSchemaStr);
        IndexedRecord outputRecord = new GenericData.Record(outputRecordSchema);
        for (AccumulatorElement accumulatorElement : accumulator.accumulatorElements) {
            IndexedRecord outputFieldRecord = accumulatorElement.extractOutput();
            if (outputFieldRecord != null) {
                outputRecord = KeyValueUtils.mergeIndexedRecord(outputFieldRecord, outputRecord, outputRecordSchema);
            }
        }
        return outputRecord;
    }

    // Implements Serializable to make sure use SerializableCoder
    public static class AggregateAccumulator implements Serializable {

        // for merge the final output record, init by first coming record
        private String outputRecordSchemaStr;

        // based on the defined operationType group
        private List<AccumulatorElement> accumulatorElements;

        public AggregateAccumulator(AggregateProperties properties) {
            List<AccumulatorElement> accs = new ArrayList();
            for (AggregateOperationProperties funcProps : properties.filteredOperations()) {
                accs.add(new AccumulatorElement(funcProps));
            }
            this.accumulatorElements = accs;
        }
    }

    public static class AccumulatorElement implements Serializable {

        AggregateOperationProperties optProps;

        String inputFieldPath;

        String outputFieldPath;

        AggregateFieldOperationType operationType;

        // init by first coming record
        AccumulatorFn accumulatorFn;

        String outputFieldSchemaStr;

        public AccumulatorElement(AggregateOperationProperties optProps) {
            this.optProps = optProps;
            this.inputFieldPath = optProps.fieldPath.getValue();
            this.outputFieldPath = AggregateUtils.genOutputFieldPath(optProps);
            this.operationType = optProps.operation.getValue();
        }

        public void addInput(IndexedRecord inputRecord) {
            if (this.outputFieldSchemaStr == null) {
                this.outputFieldSchemaStr =
                        AggregateUtils.genOutputFieldSchema(inputRecord.getSchema(), optProps).toString();
            }
            GenericData.Record inputField = KeyValueUtils.getField(inputFieldPath, inputRecord);
            if (accumulatorFn == null) {
                Schema inputFieldSchema = SchemaGeneratorUtils
                        .retrieveFieldFromJsonPath(inputRecord.getSchema(), this.inputFieldPath)
                        .schema();
                accumulatorFn = getProperCombineFn(inputFieldSchema, operationType);
                accumulatorFn.createAccumulator();
            }
            accumulatorFn.addInput(inputField);
        }

        public void mergeAccumulators(Iterable<AccumulatorElement> accumulators) {
            Iterator<AccumulatorElement> iterator = accumulators.iterator();
            if (iterator.hasNext()) {
                List accs = new ArrayList();
                while (iterator.hasNext()) {
                    AccumulatorElement next = iterator.next();
                    if (this.outputFieldSchemaStr == null) {
                        this.outputFieldSchemaStr = next.outputFieldSchemaStr;
                    }
                    if (this.accumulatorFn == null) {
                        this.accumulatorFn = next.accumulatorFn;
                        continue;
                    }
                    if (next.accumulatorFn != null) { // the next.addInput never be invoked
                        accs.add(next.accumulatorFn.getAccumulators());
                    }
                }
                this.accumulatorFn.mergeAccumulators(accs);
            }
        }

        public IndexedRecord extractOutput() {
            Schema.Parser parser = new Schema.Parser();
            Schema outputFieldSchema = parser.parse(outputFieldSchemaStr);
            GenericData.Record outputFieldRecord = new GenericData.Record(outputFieldSchema);
            AggregateUtils.setField(outputFieldPath, this.accumulatorFn.extractOutput(), outputFieldRecord);
            return outputFieldRecord;
        }

        private AccumulatorFn getProperCombineFn(Schema inputFieldSchema, AggregateFieldOperationType operationType) {
            inputFieldSchema = AvroUtils.unwrapIfNullable(inputFieldSchema);
            switch (operationType) {

            case LIST:
                return new ListAccumulatorFn();
            case COUNT:
                return new CountAccumulatorFn();
            case SUM:
                if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._int())
                        || AvroUtils.isSameType(inputFieldSchema, AvroUtils._long())) {
                    return new SumLongAccumulatorFn();
                } else if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._float())
                        || AvroUtils.isSameType(inputFieldSchema, AvroUtils._double())) {
                    return new SumDoubleAccumulatorFn();
                }
            case AVG:
                return new AvgAccumulatorFn();
            case MIN:
                if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._int())) {
                    return new MinIntegerAccumulatorFn();
                } else if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._long())) {
                    return new MinLongAccumulatorFn();
                } else if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._float())) {
                    return new MinFloatAccumulatorFn();
                } else if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._double())) {
                    return new MinDoubleAccumulatorFn();
                }
            case MAX:
                if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._int())) {
                    return new MaxIntegerAccumulatorFn();
                } else if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._long())) {
                    return new MaxLongAccumulatorFn();
                } else if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._float())) {
                    return new MaxFloatAccumulatorFn();
                } else if (AvroUtils.isSameType(inputFieldSchema, AvroUtils._double())) {
                    return new MaxDoubleAccumulatorFn();
                }
            }
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).throwIt();
            return null;
        }
    }

    public interface AccumulatorFn<AccumT, OutputT> extends Serializable {

        public void createAccumulator();

        public void addInput(GenericData.Record inputValue);

        public void mergeAccumulators(Iterable<AccumT> accsList);

        public AccumT getAccumulators();

        public OutputT extractOutput();

    }

    public static class AvgAcc implements Serializable {

        Double sum;

        Long count = 0l;
    }

    public static class AvgAccumulatorFn implements AccumulatorFn<AvgAcc, Double> {

        AvgAcc accs;

        @Override
        public void createAccumulator() {
            accs = new AvgAcc();
        }

        @Override
        public void addInput(GenericData.Record inputValue) {
            if (inputValue != null && inputValue.get(0) != null) {
                Double value = Double.valueOf(String.valueOf(inputValue.get(0)));
                accs.sum = accs.sum == null ? value : accs.sum + value;
            }
            accs.count += 1;
        }

        @Override
        public void mergeAccumulators(Iterable<AvgAcc> accsList) {
            for (AvgAcc acc : accsList) {
                if (acc.sum != null) {
                    accs.sum = accs.sum == null ? acc.sum : accs.sum + acc.sum;
                }
                this.accs.count += acc.count;
            }
        }

        @Override
        public AvgAcc getAccumulators() {
            return accs;
        }

        @Override
        public Double extractOutput() {
            return accs.sum == null ? null : (accs.count == 0l ? 0.0 : accs.sum / accs.count);
        }

    }

    public static abstract class AccumulatorFnAbstract<T> implements AccumulatorFn<T, T> {

        T accs;

        @Override
        public void createAccumulator() {
        }

        @Override
        public void addInput(GenericData.Record inputValue) {
            if (inputValue != null && inputValue.get(0) != null) {
                T value = convertFromObject(inputValue.get(0));
                accs = accs == null ? value : apply(value, accs);
            }
        }

        @Override
        public void mergeAccumulators(Iterable<T> accsList) {
            for (T acc : accsList) {
                if (acc == null) {
                    continue;
                }
                accs = accs == null ? acc : apply(acc, accs);
            }
        }

        @Override
        public T getAccumulators() {
            return accs;
        }

        @Override
        public T extractOutput() {
            return accs;
        }

        public abstract T convertFromObject(Object inputValue);

        public abstract T apply(T left, T right);

    }

    public static class SumDoubleAccumulatorFn extends AccumulatorFnAbstract<Double> {

        @Override
        public Double convertFromObject(Object inputValue) {
            return Double.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Double apply(Double left, Double right) {
            return left + right;
        }

    }

    public static class SumLongAccumulatorFn extends AccumulatorFnAbstract<Long> {

        @Override
        public Long convertFromObject(Object inputValue) {
            return Long.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Long apply(Long left, Long right) {
            return left + right;
        }

    }

    public static class CountAccumulatorFn implements AccumulatorFn<Long, Long> {

        Long accs;

        @Override
        public void createAccumulator() {
            accs = 0l;
        }

        @Override
        public void addInput(GenericData.Record inputValue) {
            accs += 1;
        }

        @Override
        public void mergeAccumulators(Iterable<Long> accsList) {
            Iterator<Long> iterator = accsList.iterator();
            while (iterator.hasNext()) {
                accs += iterator.next();
            }
        }

        @Override
        public Long getAccumulators() {
            return accs;
        }

        @Override
        public Long extractOutput() {
            return accs;
        }

    }

    public static class ListAccumulatorFn implements AccumulatorFn<List, List> {

        List<GenericData.Record> accs;

        String avroSchemaStr;

        @Override
        public void createAccumulator() {
            accs = new ArrayList();
        }

        @Override
        public void addInput(GenericData.Record inputValue) {
            if (avroSchemaStr == null && inputValue != null) {
                avroSchemaStr = inputValue.getSchema().toString();
            }
            accs.add(inputValue);
        }

        @Override
        public void mergeAccumulators(Iterable<List> accsList) {
            for (List acc : accsList) {
                accs.addAll(acc);
            }
        }

        @Override
        public List getAccumulators() {
            return accs;
        }

        @Override
        public List extractOutput() {
            List<Object> result = new ArrayList<>();
            for (GenericData.Record record : accs) {
                result.add(record == null ? null : record.get(0));
            }
            return result;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            StringUtf8Coder.of().encode(avroSchemaStr, out);
            getAccumulatorCoder().encode(this.accs, out);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            this.avroSchemaStr = StringUtf8Coder.of().decode(in);
            this.accs = getAccumulatorCoder().decode(in);
        }

        public Coder<List> getAccumulatorCoder() {
            AvroCoder valueCoder = null;
            if (avroSchemaStr != null) {
                valueCoder = AvroCoder.of(new Schema.Parser().parse(avroSchemaStr));
            }
            return (Coder<List>) (avroSchemaStr == null ? ListCoder.of(NullableCoder.of(StringUtf8Coder.of()))
                    : ListCoder.of(NullableCoder.of(valueCoder)));
        }
    }

    public static class MinIntegerAccumulatorFn extends AccumulatorFnAbstract<Integer> {

        @Override
        public Integer convertFromObject(Object inputValue) {
            return Integer.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Integer apply(Integer left, Integer right) {
            return left < right ? left : right;
        }

    }

    public static class MinLongAccumulatorFn extends AccumulatorFnAbstract<Long> {

        @Override
        public Long convertFromObject(Object inputValue) {
            return Long.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Long apply(Long left, Long right) {
            return left < right ? left : right;
        }

    }

    public static class MinFloatAccumulatorFn extends AccumulatorFnAbstract<Float> {

        @Override
        public Float convertFromObject(Object inputValue) {
            return Float.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Float apply(Float left, Float right) {
            return left < right ? left : right;
        }

    }

    public static class MinDoubleAccumulatorFn extends AccumulatorFnAbstract<Double> {

        @Override
        public Double convertFromObject(Object inputValue) {
            return Double.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Double apply(Double left, Double right) {
            return left < right ? left : right;
        }

    }

    public static class MaxIntegerAccumulatorFn extends AccumulatorFnAbstract<Integer> {

        @Override
        public Integer convertFromObject(Object inputValue) {
            return Integer.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Integer apply(Integer left, Integer right) {
            return left > right ? left : right;
        }

    }

    public static class MaxLongAccumulatorFn extends AccumulatorFnAbstract<Long> {

        @Override
        public Long convertFromObject(Object inputValue) {
            return Long.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Long apply(Long left, Long right) {
            return left > right ? left : right;
        }

    }

    public static class MaxFloatAccumulatorFn extends AccumulatorFnAbstract<Float> {

        @Override
        public Float convertFromObject(Object inputValue) {
            return Float.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Float apply(Float left, Float right) {
            return left > right ? left : right;
        }

    }

    public static class MaxDoubleAccumulatorFn extends AccumulatorFnAbstract<Double> {

        @Override
        public Double convertFromObject(Object inputValue) {
            return Double.valueOf(String.valueOf(inputValue));
        }

        @Override
        public Double apply(Double left, Double right) {
            return left > right ? left : right;
        }

    }

}
