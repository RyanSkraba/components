package org.talend.components.cassandra.io.bd;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.*;
import org.talend.components.api.component.runtime.io.Reader;
import org.talend.components.api.component.runtime.io.SingleSplit;
import org.talend.components.api.component.runtime.io.Source;
import org.talend.components.api.component.runtime.io.Split;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.TypeMapping;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.row.BaseRowStruct;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bchen on 16-1-10.
 */
//TODO better to and a talend InputFormat to avoid the dependency of MapReduce, just need a method getSplits;no close method for InputFormat?
public class BDInputFormat implements InputFormat<NullWritable, BaseRowStruct>, JobConfigurable {
    private Source source;

    @Override
    public InputSplit[] getSplits(JobConf jobConf, int num) throws IOException {
        if (source.supportSplit()) {
            Split[] splits = source.getSplit(num);
            BDInputSplit[] bdInputSplits = new BDInputSplit[splits.length];
            for (int i = 0; i < bdInputSplits.length; i++) {
                bdInputSplits[i] = new BDInputSplit(splits[i]);
            }
            return bdInputSplits;
        } else {
            return new BDInputSplit[]{new BDInputSplit(new SingleSplit())};
        }
    }

    @Override
    public RecordReader<NullWritable, BaseRowStruct> getRecordReader(InputSplit inputSplit, JobConf jobConf, Reporter reporter) throws IOException {
        return new BDRecordReader(source.getRecordReader(((BDInputSplit) inputSplit).getRealSplit()), source.getSchema(), source.getFamilyName());
    }

    @Override
    public void configure(JobConf jobConf) {
        try {
            Class<? extends Source> aClass = (Class<? extends Source>) Class.forName(jobConf.get("input.source"));
            this.source = aClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String componentPropertiesString = jobConf.get("input.props");
        ComponentProperties.Deserialized deserialized = ComponentProperties.fromSerialized(componentPropertiesString);
        ComponentProperties properties = deserialized.properties;
        this.source.init(properties);
    }

    static class BDRecordReader implements RecordReader<NullWritable, BaseRowStruct> {
        private Reader reader;
        private List<SchemaElement> schema;
        private String familyName;

        BDRecordReader(Reader reader, List<SchemaElement> schema, String familyName) {
            this.reader = reader;
            this.schema = schema;
            this.familyName = familyName;
        }

        @Override
        public boolean next(NullWritable nullWritable, BaseRowStruct baseRowStruct) throws IOException {
            if (reader.advance()) {
                Object row = reader.getCurrent();

                for (SchemaElement column : schema) {
                    DataSchemaElement col = (DataSchemaElement) column;
                    try {
                        baseRowStruct.put(col.getName(), TypeMapping.convert(TypeMapping.getDefaultTalendType(familyName, col.getAppColType()),
                                col.getType(), col.getAppColType().newInstance().retrieveTValue(row, col.getAppColName())));
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public NullWritable createKey() {
            return NullWritable.get();
        }

        @Override
        public BaseRowStruct createValue() {
            Map<String, SchemaElement.Type> row_metadata = new HashMap<>();
            for (SchemaElement field : schema) {
                row_metadata.put(field.getName(), field.getType());
            }
            return new BaseRowStruct(row_metadata);
        }

        @Override
        public long getPos() throws IOException {
            return 0;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        @Override
        public float getProgress() throws IOException {
            return 0;
        }
    }

    static class BDInputSplit implements InputSplit, Comparable<BDInputSplit> {
        private Split split;

        public BDInputSplit() {
            this(new SingleSplit());
        }

        public BDInputSplit(Split split) {
            this.split = split;
        }

        public Split getRealSplit() {
            return split;
        }

        @Override
        public int compareTo(BDInputSplit o) {
            return split.compareTo((Split) o);
        }

        @Override
        public long getLength() throws IOException {
            return split.getLength();
        }

        @Override
        public String[] getLocations() throws IOException {
            return split.getLocations();
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            split.write(dataOutput);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            split.readFields(dataInput);
        }
    }
}
