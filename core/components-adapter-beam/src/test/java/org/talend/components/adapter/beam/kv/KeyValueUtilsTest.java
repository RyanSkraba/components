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
package org.talend.components.adapter.beam.kv;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.daikon.avro.GenericDataRecordHelper;

public class KeyValueUtilsTest {

    private final Schema inputSimpleSchema = SchemaBuilder.record("inputRow") //
            .fields() //
            .name("a").type().optional().stringType() //
            .name("b").type().optional().stringType() //
            .name("c").type().optional().stringType() //
            .endRecord();

    private final Schema inputHierarchicalSchema = SchemaBuilder.record("inputRow") //
            .fields() //
            .name("name").type().optional().stringType() //
            .name("data").type().optional().record("data").fields().name("a").type().optional().stringType() //
            .name("b").type().optional().stringType() //
            .name("c").type().optional().stringType() //
            .endRecord().endRecord();

    /**
     * From the input: {"a": "a", "b": "b", "c": "c"}
     * 
     * Extract elements: "a", "b" and "c"
     * 
     * The result should be:
     * 
     * key: {"a": "a", "b": "b", "c": "c"}
     * 
     * value: null
     */
    @Test
    public void test_EverythingIsAKey() throws Exception {
        GenericRecord inputRecord = new GenericRecordBuilder(inputSimpleSchema) //
                .set("a", "a") //
                .set("b", "b") //
                .set("c", "c") //
                .build();

        List<String> keyList = Arrays.asList("a", "b", "c");

        String transformedIndexedRecord = ("{'key': {'a': 'a', 'b': 'b', 'c': 'c'}, " + "'value': {}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputRecord,
                SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'a': 'a', 'b': 'b', 'c': 'c'}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input: {"a": "a", "b": "b", "c": "c"}
     * 
     * no extracted element.
     * 
     * The result should be:
     * 
     * key: empty
     * 
     * value: {"a": "a", "b": "b", "c": "c"}
     */
    @Test
    public void test_EverythingIsAValue() throws Exception {
        GenericRecord inputRecord = new GenericRecordBuilder(inputSimpleSchema) //
                .set("a", "a") //
                .set("b", "b") //
                .set("c", "c") //
                .build();

        String transformedIndexedRecord = ("{'key': {}, " + "'value': {'a': 'a', 'b': 'b', 'c': 'c'}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputRecord,
                SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), new ArrayList<String>()));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'a': 'a', 'b': 'b', 'c': 'c'}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input: {"a": "a", "b": "b", "c": "c"}
     * 
     * Extract elements: "c", "a" and "d"
     * 
     * The result should be:
     * 
     * key: {"c": "c", "a": "a", "d": null}
     * 
     * value: {""b", "b""}
     */
    @Test
    public void test_SimpleLevel() throws Exception {
        GenericRecord inputRecord = new GenericRecordBuilder(inputSimpleSchema) //
                .set("a", "a") //
                .set("b", "b") //
                .set("c", "c") //
                .build();

        List<String> keyList = Arrays.asList("c", "a", "d");

        String transformedIndexedRecord = ("{'key': {'c': 'c', 'a': 'a', 'd': null}, " + "'value': {'b': 'b'}}").replaceAll("\\'",
                "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputRecord,
                SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'c': 'c', 'a': 'a', 'd': null, 'b': 'b'}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input: {"name": "testdata", "data": {"a": "a", "b": "b", "c": "c"}}
     * 
     * Extract elements: "name", "data.a", "data.b" and "data.c"
     * 
     * The result should be:
     * 
     * key: {"name": "testdata", "data": {"a": "a", "b": "b", "c": "c"}}
     * 
     * value: null
     */
    @Test
    public void test_Hierarchical_EverythingIsAKey() throws Exception {
        GenericRecord inputRecord = new GenericRecordBuilder(inputHierarchicalSchema) //
                .set("name", "testdata") //
                .build();
        inputRecord.put("data",
                new GenericRecordBuilder(inputSimpleSchema) //
                        .set("a", "a") //
                        .set("b", "b") //
                        .set("c", "c") //
                        .build());

        List<String> keyList = Arrays.asList("name", "data");

        String transformedIndexedRecord = ("{'key': {'name': 'testdata', 'data': {'a': 'a', 'b': 'b', 'c': 'c'}}, "
                + "'value': {}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputRecord,
                SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'name': 'testdata', 'data': {'a': 'a', 'b': 'b', 'c': 'c'}}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input: {"name": "testdata", "data": {"a": "a", "b": "b", "c": "c"}}
     * 
     * No extracted element.
     * 
     * The result should be:
     * 
     * key: empty
     * 
     * value: {"name": "testdata", "data": {"a": "a", "b": "b", "c": "c"}}
     */
    @Test
    public void test_Hierarchical_EverythingIsAValue() throws Exception {
        GenericRecord inputRecord = new GenericRecordBuilder(inputHierarchicalSchema) //
                .set("name", "testdata") //
                .build();
        inputRecord.put("data",
                new GenericRecordBuilder(inputSimpleSchema) //
                        .set("a", "a") //
                        .set("b", "b") //
                        .set("c", "c") //
                        .build());

        String transformedIndexedRecord = ("{'key': {}, "
                + "'value': {'name': 'testdata', 'data': {'a': 'a', 'b': 'b', 'c': 'c'}}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputRecord,
                SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), new ArrayList<String>()));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'name': 'testdata', 'data': {'a': 'a', 'b': 'b', 'c': 'c'}}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input: {"name": "testdata", "data": {"a": "a", "b": "b", "c": "c"}}
     * 
     * Extract elements: "name", "data.c", "data.a" and "data.d"
     * 
     * The result should be:
     * 
     * key: {"name": "testdata", "data": {"c": "c", "a": "a", "d": null}}
     * 
     * value: {"data": {"b": "b"}}
     */
    @Test
    public void test_Hierarchical_onelevel() throws Exception {
        GenericRecord inputRecord = new GenericRecordBuilder(inputHierarchicalSchema) //
                .set("name", "testdata") //
                .build();
        inputRecord.put("data",
                new GenericRecordBuilder(inputSimpleSchema) //
                        .set("a", "a") //
                        .set("b", "b") //
                        .set("c", "c") //
                        .build());

        List<String> keyList = Arrays.asList("name", "data.c", "data.a", "data.d");

        String transformedIndexedRecord = ("{'key': {'name': 'testdata', 'data': {'c': 'c', 'a': 'a', 'd': null}}, "
                + "'value': {'data': {'b': 'b'}}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputRecord,
                SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'name': 'testdata', 'data': {'c': 'c', 'a': 'a', 'd': null, 'b': 'b'}}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input: {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1", "c2": "B-C-c2"}}}
     * 
     * Extract elements: "a1", "B.C.D.d1" and "B.C.c1"
     * 
     * The result should be:
     * 
     * key: {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1"}}}
     * 
     * value: {"B": {"C": {"c2": "B-C-c2"}}}
     */
    @Test
    public void test_hierarchical_multilevel_simple() throws Exception {
        Object[] inputAsObject = new Object[] { "a1",
                new Object[] { new Object[] { new Object[] { "B-C-D-d1" }, "B-C-c1", "B-C-c2" } } };
        IndexedRecord inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        List<String> keyList = Arrays.asList("a1", "B.C.D.d1", "B.C.c1");

        String transformedIndexedRecord = ("{'key': {'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1'}}}, "
                + "'value': {'B': {'C': {'c2': 'B-C-c2'}}}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputIndexedRecord,
                SchemaGeneratorUtils.extractKeyValues(inputIndexedRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1', 'c2': 'B-C-c2'}}}")
                .replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input:
     * 
     * {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1", "c2": "B-C-c2"}, "E": {"F": {"f1": "B-E-F-F1"},
     * "e1": "B-E-e1", "e2": "B-E-e2"}}}
     * 
     * Extract elements: "a1" and "B"
     * 
     * The result should be:
     * 
     * key: {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1", "c2": "B-C-c2"}, "E": {"F": {"f1":
     * "B-E-F-F1"}, "e1": "B-E-e1", "e2": "B-E-e2"}}}
     * 
     * value: empty
     */
    @Test
    public void test_hierarchical_multilevel_duplicate_subpart() throws Exception {
        Object[] inputAsObject = new Object[] { "a1",
                new Object[] { new Object[] { new Object[] { "B-C-D-d1" }, "B-C-c1", "B-C-c2" },
                        new Object[] { new Object[] { "B-E-F-f1" }, "B-E-e1", "B-E-e2" } } };

        List<String> keyList = Arrays.asList("a1", "B");

        IndexedRecord inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        String transformedIndexedRecord = ("{'key': {'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1', 'c2': 'B-C-c2'}, "
                + "'E': {'F': {'f1': 'B-E-F-f1'}, 'e1': 'B-E-e1', 'e2': 'B-E-e2'}}}, 'value': {}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputIndexedRecord,
                SchemaGeneratorUtils.extractKeyValues(inputIndexedRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1', 'c2': 'B-C-c2'}, "
                + "'E': {'F': {'f1': 'B-E-F-f1'}, 'e1': 'B-E-e1', 'e2': 'B-E-e2'}}}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input:
     * 
     * {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1", "c2": "B-C-c2"}, "E": {"F": {"f1": "B-E-F-F1"},
     * "e1": "B-E-e1", "e2": "B-E-e2"}}}
     * 
     * Extract elements: "a1", "B.C" and "B.E"
     * 
     * The result should be:
     * 
     * key: {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1", "c2": "B-C-c2"}, "E": {"F": {"f1":
     * "B-E-F-F1"}, "e1": "B-E-e1", "e2": "B-E-e2"}}}
     * 
     * value: empty
     */
    @Test
    public void test_hierarchical_multilevel_duplicate_subpart2() throws Exception {
        Object[] inputAsObject = new Object[] { "a1",
                new Object[] { new Object[] { new Object[] { "B-C-D-d1" }, "B-C-c1", "B-C-c2" },
                        new Object[] { new Object[] { "B-E-F-f1" }, "B-E-e1", "B-E-e2" } } };

        List<String> keyList = Arrays.asList("a1", "B.C", "B.E");

        IndexedRecord inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        String transformedIndexedRecord = ("{'key': {'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1', 'c2': 'B-C-c2'}, "
                + "'E': {'F': {'f1': 'B-E-F-f1'}, 'e1': 'B-E-e1', 'e2': 'B-E-e2'}}}, 'value': {}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputIndexedRecord,
                SchemaGeneratorUtils.extractKeyValues(inputIndexedRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1', 'c2': 'B-C-c2'}, "
                + "'E': {'F': {'f1': 'B-E-F-f1'}, 'e1': 'B-E-e1', 'e2': 'B-E-e2'}}}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input:
     * 
     * {"B": {"C": {"c1": "B.C.c1"}}}
     * 
     * Extract elements: "B.C.c1", "B.C.c2", "B.b1", "a1", "D.d1" and "E.F.f1"
     * 
     * The result should be:
     * 
     * key: {"B": {"C": {"c1": "B.C.c1", "c2": null}, "b1": null}, "a1": null, "D": {"d1": null}, "E": {"F": {"e1": null
     * }}}
     * 
     * value: empty
     */
    @Test
    public void test_hierarchical_multilevel_createNullKeys() throws Exception {
        Object[] inputAsObject = new Object[] { new Object[] { new Object[] { "B.C.c1" } } };
        IndexedRecord inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject);
        List<String> keyList = Arrays.asList("B.C.c1", "B.C.c2", "B.b1", "a1", "D.d1", "E.F.f1");

        String transformedIndexedRecord = ("{'key': {'B': {'C': {'c1': 'B.C.c1', 'c2': null}, 'b1': null}, 'a1': null, 'D': {'d1': null}, "
                + "'E': {'F': {'f1': null}}}, " + "'value': {}}").replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputIndexedRecord,
                SchemaGeneratorUtils.extractKeyValues(inputIndexedRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'B': {'C': {'c1': 'B.C.c1', 'c2': null}, 'b1': null}, 'a1': null, 'D': {'d1': null}, "
                + "'E': {'F': {'f1': null}}}").replaceAll("\\'", "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }

    /**
     * From the input:
     * 
     * {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1", "c2": "B-C-c2"}, "E": {"F": {"f1": "B-E-F-f1"},
     * "e1": "B-E-e1", "e2": "B-E-e2"}}, "G": {"H": {"h1": "B-G-H-h1", "I": {"i1": "B-G-H-I-i1"}}}}
     * 
     * Extract elements: "a1", "B.C.D.d1", "B.C.c1", "B.E", "G.H.h1", "a2" and "I.i1"
     * 
     * The result should be:
     * 
     * key: {"a1": "a1", "B": {"C": {"D": {"d1": "B-C-D-d1"}, "c1": "B-C-c1"}, "E": {"F": {"f1": "B-E-F-f1"}, "e1":
     * "B-E-e1", "e2": "B-E-e2"}}, "G": {"H": {"h1": "B-G-H-h1"}}, "a2": null, "I": {"i1": null}}
     * 
     * value: {"B": {"C": {"c2": "B-C-c2"}}, "G": {"H": {"I": {"i1": "B-G-H-I-i1"}}}}
     */
    @Test
    public void test_hierarchical_multilevel() throws Exception {
        Object[] inputAsObject = new Object[] { "a1",
                new Object[] { new Object[] { new Object[] { "B-C-D-d1" }, "B-C-c1", "B-C-c2" },
                        new Object[] { new Object[] { "B-E-F-f1" }, "B-E-e1", "B-E-e2" } },
                new Object[] { new Object[] { "B-G-H-h1", new Object[] { "B-G-H-I-i1" } } } };
        IndexedRecord inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        List<String> keyList = Arrays.asList("a1", "B.C.D.d1", "B.C.c1", "B.E", "G.H.h1", "a2", "I.i1");

        String transformedIndexedRecord = ("{'key': {'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1'}, "
                + "'E': {'F': {'f1': 'B-E-F-f1'}, 'e1': 'B-E-e1', 'e2': 'B-E-e2'}}, "
                + "'G': {'H': {'h1': 'B-G-H-h1'}}, 'a2': null, "
                + "'I': {'i1': null}}, 'value': {'B': {'C': {'c2': 'B-C-c2'}}, 'G': {'H': {'I': {'i1': 'B-G-H-I-i1'}}}}}")
                        .replaceAll("\\'", "\"");
        IndexedRecord outputRecord = KeyValueUtils.transformToKV(inputIndexedRecord,
                SchemaGeneratorUtils.extractKeyValues(inputIndexedRecord.getSchema(), keyList));
        assertEquals(transformedIndexedRecord, outputRecord.toString());

        Schema kvSchema = SchemaGeneratorUtils.mergeKeyValues(outputRecord.getSchema());
        String mergedRecord = ("{'a1': 'a1', 'B': {'C': {'D': {'d1': 'B-C-D-d1'}, 'c1': 'B-C-c1', 'c2': 'B-C-c2'}, "
                + "'E': {'F': {'f1': 'B-E-F-f1'}, 'e1': 'B-E-e1', 'e2': 'B-E-e2'}}, "
                + "'G': {'H': {'h1': 'B-G-H-h1', 'I': {'i1': 'B-G-H-I-i1'}}}, 'a2': null, 'I': {'i1': null}}").replaceAll("\\'",
                        "\"");
        assertEquals(mergedRecord, KeyValueUtils.transformFromKV(outputRecord, kvSchema).toString());
    }
}
