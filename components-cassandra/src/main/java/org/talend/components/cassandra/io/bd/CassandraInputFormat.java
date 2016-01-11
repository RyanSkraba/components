package org.talend.components.cassandra.io.bd;

import com.datastax.driver.core.Row;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.*;
import org.talend.components.api.component.io.Reader;
import org.talend.components.api.component.io.SingleSplit;
import org.talend.components.api.component.io.Split;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.cassandra.io.CassandraSource;
import org.talend.components.cassandra.type.TEXT;
import org.talend.row.BaseRowStruct;
import org.talend.schema.Column;
import org.talend.schema.type.TBaseType;
import org.talend.schema.type.TString;
import org.talend.schema.type.TypeMapping;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

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
        return new CassandraRecordReader(cassandraSource.getRecordReader(((CassandraSplit) inputSplit).getRealSplit()));
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
        private Reader<Row> reader;

        CassandraRecordReader(Reader<Row> reader) {
            this.reader = reader;
        }

        @Override
        public boolean next(NullWritable nullWritable, BaseRowStruct baseRowStruct) throws IOException {
            if (reader.advance()) {
                //TODO avoid Row
                Row row = reader.getCurrent();
                //TODO metadata should be the schmea and get it from properties
//                List<Column> metadata = new ArrayList<>();
//                Column col1 = new Column(false, "id", TEXT.class);
//                Column col2 = new Column(false, "birthday", TIMESTAMP.class);
//                Column col3 = new Column(false, "age", INT.class);
//                metadata.addAll(Arrays.asList(new Column[] { col1, col2, col3 }));
//                metadata.get(0).setTalendType("id", TString.class);
//                metadata.get(1).setTalendType("birthday", TDate.class);
//                metadata.get(2).setTalendType("age", TInt.class);

                //TODO metadata should be the schmea and get it from properties
                List<Column> metadata = new ArrayList<>();
                Column col1 = new Column(false, "name", TEXT.class);
                metadata.addAll(Arrays.asList(new Column[]{col1}));
                metadata.get(0).setTalendType("name", TString.class);
                //TODO to support (app,key); (app,position); (value), now only (app,key)
                for (Column column : metadata) {
                    try {
                        baseRowStruct.put(column.getCol_name(), TypeMapping.convert(column.getApp_col_type().newInstance().getDefaultTalendType(),
                                column.getCol_type(), column.getApp_col_type().newInstance().retrieveTValue(row, column.getApp_col_name())));
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
            Map<String, Class<? extends TBaseType>> metadata = new HashMap<>();
            metadata.put("name", TString.class);
            return new BaseRowStruct(metadata);
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
