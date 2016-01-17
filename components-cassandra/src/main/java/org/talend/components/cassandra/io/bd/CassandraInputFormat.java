package org.talend.components.cassandra.io.bd;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.*;
import org.talend.components.api.component.runtime.io.Reader;
import org.talend.components.api.component.runtime.io.SingleSplit;
import org.talend.components.api.component.runtime.io.Split;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.TypeMapping;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.cassandra.io.CassandraSource;
import org.talend.components.cassandra.type.CassandraBaseType;
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
public class CassandraInputFormat implements InputFormat<NullWritable, BaseRowStruct>, JobConfigurable {
    private CassandraSource cassandraSource;

    @Override
    public InputSplit[] getSplits(JobConf jobConf, int num) throws IOException {
        if (cassandraSource.supportSplit()) {
            Split[] splits = cassandraSource.getSplit(num);
            CassandraSplit[] cassandraSplits = new CassandraSplit[splits.length];
            for (int i = 0; i < cassandraSplits.length; i++) {
                cassandraSplits[i] = new CassandraSplit(splits[i]);
            }
            return cassandraSplits;
        } else {
            return new CassandraSplit[]{new CassandraSplit(new SingleSplit())};
        }
    }

    @Override
    public RecordReader<NullWritable, BaseRowStruct> getRecordReader(InputSplit inputSplit, JobConf jobConf, Reporter reporter) throws IOException {
        return new CassandraRecordReader(cassandraSource.getRecordReader(((CassandraSplit) inputSplit).getRealSplit()), cassandraSource.getSchema());
    }

    @Override
    public void configure(JobConf jobConf) {
        cassandraSource = new CassandraSource();
        String componentPropertiesString = jobConf.get("componentProperties");//TODO cid! then make CassandraSource to Source
        ComponentProperties.Deserialized deserialized = ComponentProperties.fromSerialized(componentPropertiesString);
        ComponentProperties properties = deserialized.properties;
        cassandraSource.init(properties);
    }

    static class CassandraRecordReader implements RecordReader<NullWritable, BaseRowStruct> {
        private Reader reader;
        private List<SchemaElement> schema;

        CassandraRecordReader(Reader reader, List<SchemaElement> schema) {
            this.reader = reader;
            this.schema = schema;
        }

        @Override
        public boolean next(NullWritable nullWritable, BaseRowStruct baseRowStruct) throws IOException {
            if (reader.advance()) {
                Object row = reader.getCurrent();

                for (SchemaElement column : schema) {
                    DataSchemaElement col = (DataSchemaElement) column;
                    try {
                        baseRowStruct.put(col.getName(), TypeMapping.convert(TypeMapping.getDefaultTalendType(CassandraBaseType.FAMILY_NAME, col.getAppColType()),
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

    static class CassandraSplit implements InputSplit, Comparable<CassandraSplit> {
        private Split split;

        public CassandraSplit() {
            this(new SingleSplit());
        }

        public CassandraSplit(Split split) {
            this.split = split;
        }

        public Split getRealSplit() {
            return split;
        }

        @Override
        public int compareTo(CassandraSplit o) {
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
