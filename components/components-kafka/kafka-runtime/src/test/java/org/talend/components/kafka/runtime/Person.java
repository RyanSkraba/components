// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.kafka.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;

public class Person {

    public static final Schema schema;

    static {
        SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record("row").namespace("person").fields();
        fields = fields.name("group").type(Schema.create(Schema.Type.STRING)).noDefault();
        fields = fields.name("name").type(Schema.create(Schema.Type.STRING)).noDefault();
        fields = fields.name("age").type(Schema.create(Schema.Type.INT)).noDefault();
        fields = fields.name("gender").type(Schema.create(Schema.Type.BOOLEAN)).noDefault();
        schema = fields.endRecord();
    }

    // group used when you try to send different test data into same topic, you can use group as a filter
    public String group;

    public String name;

    public Integer age;

    public Boolean gender;

    public Person(String group, String name, Integer age, Boolean gender) {
        this.group = group;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public static Person genRandom(String group, int randomSeed) {
        Random random = new Random();
        return new Person(group, "name" + random.nextInt(randomSeed), random.nextInt(randomSeed), random.nextBoolean());
    }

    public static List<Person> genRandomList(String group, int size) {
        List<Person> personGroup = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            personGroup.add(genRandom(group, size));
        }
        return personGroup;
    }

    public static Person fromCSV(String record, String fieldDelimiter) {
        String[] fields = record.split(fieldDelimiter);
        return new Person(fields[0], fields[1], Integer.valueOf(fields[2]), Boolean.valueOf(fields[3]));
    }

    public static Person desFromAvroBytes(byte[] record) throws IOException {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        BinaryDecoder decoder = null;
        decoder = DecoderFactory.get().binaryDecoder(record, decoder);
        GenericRecord avroValue = datumReader.read(null, decoder);
        return fromAvroRecord(avroValue);
    }

    public static Person fromAvroRecord(IndexedRecord record) {
        return new Person(String.valueOf(record.get(record.getSchema().getField("group").pos())),
                String.valueOf(record.get(record.getSchema().getField("name").pos())),
                Integer.valueOf(String.valueOf(record.get(record.getSchema().getField("age").pos()))),
                Boolean.valueOf(String.valueOf(record.get(record.getSchema().getField("gender").pos()))));
    }

    public String toCSV(String fieldDelimiter) {
        return group + fieldDelimiter + name + fieldDelimiter + age + fieldDelimiter + gender;
    }

    public GenericRecord toAvroRecord() {
        GenericRecord record = new GenericData.Record(schema);
        record.put("group", group);
        record.put("name", name);
        record.put("age", age);
        record.put("gender", gender);
        return record;
    }

    public byte[] serToAvroBytes() throws IOException {
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        datumWriter.write(toAvroRecord(), encoder);
        encoder.flush();
        byte[] result = out.toByteArray();
        out.close();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Person))
            return false;
        Person o = (Person) other;
        if (o.group.equals(this.group) && o.name.equals(this.name) && o.age.equals(this.age) && o.gender.equals(this.gender))
            return true;
        return false;
    }

}
