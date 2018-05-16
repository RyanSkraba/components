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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.GenericDataRecordHelper;

public class SchemaGeneratorUtilsTest {

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
     * From the input: null
     * 
     * Extract elements: null
     * 
     * The result should be:
     * 
     * key: empty
     * 
     * value: emtpy
     */
    @Test
    public void test_nullSchema() throws Exception {
        List<String> keyList = new ArrayList<String>();

        assertThat(SchemaGeneratorUtils.extractKeys(null, keyList), is(AvroUtils.createEmptySchema()));

        assertThat(SchemaGeneratorUtils.extractValues(null, keyList), is(AvroUtils.createEmptySchema()));

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(null, keyList);
        String kvSchemaExpected = ("{'type':'record','name':'keyvalue','fields':["
                + "{'name':'key','type':{'type':'record','name':'EmptySchema','fields':[]},'doc':'','default':''},"
                + "{'name':'value','type':'EmptySchema','doc':'','default':''}]}").replace('\'', '"');
        System.out.println(kvSchema);
        assertThat(kvSchema.toString(), is(kvSchemaExpected));

        assertThat(SchemaGeneratorUtils.mergeKeyValues(kvSchema), is(AvroUtils.createEmptySchema()));
    }

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

        String keyOutput = ("{'type':'record','name':'inputRow','fields':["
                + "{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},"
                + "{'name':'c','type':['null','string'],'default':null}]}").replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        assertThat(SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList),
                is(AvroUtils.createEmptySchema()));

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, AvroUtils.createEmptySchema().toString()), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'inputRow','fields':[{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},{'name':'c','type':['null','string'],'default':null}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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

        List<String> keyList = new ArrayList<String>();

        String keyOutput = ("{'type':'record','name':'inputRow','fields':[]}").replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        String valueOutput = ("{'type':'record','name':'value_inputRow','fields':["
                + "{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},"
                + "{'name':'c','type':['null','string'],'default':null}]}").replaceAll("\\'", "\"");
        assertEquals(valueOutput, SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList).toString());

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, valueOutput), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'inputRow','fields':[{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},{'name':'c','type':['null','string'],'default':null}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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

        String keyOutput = ("{'type':'record','name':'inputRow','fields':["
                + "{'name':'c','type':['null','string'],'default':null},"
                + "{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'d','type':['null','string'],'doc':'','default':''}]}").replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        String valueOutput = ("{'type':'record','name':'value_inputRow','fields':["
                + "{'name':'b','type':['null','string'],'default':null}]}").replaceAll("\\'", "\"");
        assertEquals(valueOutput, SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList).toString());

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, valueOutput), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'inputRow','fields':[{'name':'c','type':['null','string'],'default':null},"
                + "{'name':'a','type':['null','string'],'default':null},{'name':'d','type':['null','string'],'doc':'','default':''},"
                + "{'name':'b','type':['null','string'],'default':null}]}").replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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

        String keyOutput = ("{'type':'record','name':'inputRow','fields':["
                + "{'name':'name','type':['null','string'],'default':null},"
                + "{'name':'data','type':['null',{'type':'record','name':'data','fields':["
                + "{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},"
                + "{'name':'c','type':['null','string'],'default':null}]}],'default':null}]}").replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        assertThat(SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList),
                is(AvroUtils.createEmptySchema()));

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, AvroUtils.createEmptySchema().toString()), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'inputRow','fields':[{'name':'name','type':['null','string'],'default':null},"
                + "{'name':'data','type':['null',{'type':'record','name':'data','fields':[{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},{'name':'c','type':['null','string'],'default':null}]}],'default':null}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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

        List<String> keyList = new ArrayList<String>();

        String keyOutput = ("{'type':'record','name':'inputRow','fields':[]}").replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        String valueOutput = ("{'type':'record','name':'value_inputRow','fields':["
                + "{'name':'name','type':['null','string'],'default':null},"
                + "{'name':'data','type':{'type':'record','name':'value_data','fields':["
                + "{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},"
                + "{'name':'c','type':['null','string'],'default':null}]},'doc':'','default':''}]}").replaceAll("\\'", "\"");
        assertEquals(valueOutput, SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList).toString());

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, valueOutput), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'inputRow','fields':[{'name':'name','type':['null','string'],'default':null},"
                + "{'name':'data','type':{'type':'record','name':'value_data','fields':[{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'b','type':['null','string'],'default':null},{'name':'c','type':['null','string'],'default':null}]},'doc':'','default':''}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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

        String keyOutput = ("{'type':'record','name':'inputRow','fields':["
                + "{'name':'name','type':['null','string'],'default':null}," + "{'name':'data','type':{'type':'record','fields':["
                + "{'name':'c','type':['null','string'],'default':null},"
                + "{'name':'a','type':['null','string'],'default':null},"
                + "{'name':'d','type':['null','string'],'doc':'','default':''}]}," + "'doc':'','default':''}]}").replaceAll("\\'",
                        "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        String valueOutput = ("{'type':'record','name':'value_inputRow','fields':["
                + "{'name':'data','type':{'type':'record','name':'value_data','fields':["
                + "{'name':'b','type':['null','string'],'default':null}]}," + "'doc':'','default':''}]}").replaceAll("\\'", "\"");
        assertEquals(valueOutput, SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList).toString());

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, valueOutput), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'inputRow','fields':[{'name':'name','type':['null','string'],'default':null},"
                + "{'name':'data','type':{'type':'record','fields':[{'name':'c','type':['null','string'],'default':null},"
                + "{'name':'a','type':['null','string'],'default':null},{'name':'d','type':['null','string'],'doc':'','default':''},"
                + "{'name':'b','type':['null','string'],'default':null}]},'doc':'','default':''}]}").replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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
        IndexedRecord inputRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        List<String> keyList = Arrays.asList("a1", "B.C.D.d1", "B.C.c1");

        String keyOutput = ("{'type':'record','name':'InRecord','fields':[" + "{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':["
                + "{'name':'D','type':{'type':'record','name':'DRecord','fields':[{'name':'d1','type':'string'}]},'doc':'','default':''},"
                + "{'name':'c1','type':'string'}]},'doc':'','default':''}]},'doc':'','default':''}]}").replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        String valueOutput = ("{'type':'record','name':'value_InRecord','fields':["
                + "{'name':'B','type':{'type':'record','name':'value_BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'value_CRecord','fields':["
                + "{'name':'c2','type':'string'}]},'doc':'','default':''}]},'doc':'','default':''}]}").replaceAll("\\'", "\"");
        assertEquals(valueOutput, SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList).toString());

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, valueOutput), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'InRecord','fields':[{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':["
                + "{'name':'D','type':{'type':'record','name':'DRecord','fields':[{'name':'d1','type':'string'}]},'doc':'','default':''},"
                + "{'name':'c1','type':'string'},{'name':'c2','type':'string'}]},'doc':'','default':''}]},'doc':'','default':''}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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
                        new Object[] { new Object[] { "B-E-F-F1" }, "B-E-e1", "B-E-e2" } } };
        IndexedRecord inputRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        List<String> keyList = Arrays.asList("a1", "B");

        String keyOutput = ("{'type':'record','name':'InRecord','fields':[{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':[{'name':'C','type':"
                + "{'type':'record','name':'CRecord','fields':[{'name':'D','type':{'type':'record','name':'DRecord','fields':["
                + "{'name':'d1','type':'string'}]}},{'name':'c1','type':'string'},{'name':'c2','type':'string'}]}},"
                + "{'name':'E','type':{'type':'record','name':'ERecord','fields':["
                + "{'name':'F','type':{'type':'record','name':'FRecord','fields':[{'name':'f1','type':'string'}]}},"
                + "{'name':'e1','type':'string'},{'name':'e2','type':'string'}]}}]}}]}").replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        assertThat(SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList),
                is(AvroUtils.createEmptySchema()));

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, AvroUtils.createEmptySchema().toString()), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'InRecord','fields':[{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':["
                + "{'name':'D','type':{'type':'record','name':'DRecord','fields':[{'name':'d1','type':'string'}]}},"
                + "{'name':'c1','type':'string'},{'name':'c2','type':'string'}]}},"
                + "{'name':'E','type':{'type':'record','name':'ERecord','fields':["
                + "{'name':'F','type':{'type':'record','name':'FRecord','fields':[{'name':'f1','type':'string'}]}},"
                + "{'name':'e1','type':'string'},{'name':'e2','type':'string'}]}}]}}]}").replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());

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
                        new Object[] { new Object[] { "B-E-F-F1" }, "B-E-e1", "B-E-e2" } } };
        IndexedRecord inputRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        List<String> keyList = Arrays.asList("a1", "B.C", "B.E");

        String keyOutput = ("{'type':'record','name':'InRecord','fields':[{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':[{'name':'C','type':"
                + "{'type':'record','name':'CRecord','fields':[{'name':'D','type':{'type':'record','name':'DRecord','fields':["
                + "{'name':'d1','type':'string'}]}},{'name':'c1','type':'string'},{'name':'c2','type':'string'}]}},"
                + "{'name':'E','type':{'type':'record','name':'ERecord','fields':["
                + "{'name':'F','type':{'type':'record','name':'FRecord','fields':[{'name':'f1','type':'string'}]}},"
                + "{'name':'e1','type':'string'},{'name':'e2','type':'string'}]}}]},'doc':'','default':''}]}").replaceAll("\\'",
                        "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        assertThat(SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList),
                is(AvroUtils.createEmptySchema()));

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, AvroUtils.createEmptySchema().toString()), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'InRecord','fields':[{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':["
                + "{'name':'D','type':{'type':'record','name':'DRecord','fields':[{'name':'d1','type':'string'}]}},"
                + "{'name':'c1','type':'string'},{'name':'c2','type':'string'}]}},"
                + "{'name':'E','type':{'type':'record','name':'ERecord','fields':["
                + "{'name':'F','type':{'type':'record','name':'FRecord','fields':[{'name':'f1','type':'string'}]}},"
                + "{'name':'e1','type':'string'},{'name':'e2','type':'string'}]}}]},'doc':'','default':''}]}").replaceAll("\\'",
                        "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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
        IndexedRecord inputRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        List<String> keyList = Arrays.asList("B.C.c1", "B.C.c2", "B.b1", "a1", "D.d1", "E.F.f1");

        String keyOutput = ("{'type':'record','name':'InRecord','fields':[{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':["
                + "{'name':'c1','type':'string'},{'name':'c2','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'b1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''},{'name':'a1','type':['null','string'],'doc':'','default':''},"
                + "{'name':'D','type':{'type':'record','fields':[{'name':'d1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'E','type':{'type':'record','fields':[{'name':'F','type':{'type':'record','fields':["
                + "{'name':'f1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''}]},'doc':'','default':''}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        assertThat(SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList),
                is(AvroUtils.createEmptySchema()));

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, AvroUtils.createEmptySchema().toString()), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'InRecord','fields':[{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':[{'name':'c1','type':'string'},"
                + "{'name':'c2','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'b1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'a1','type':['null','string'],'doc':'','default':''},"
                + "{'name':'D','type':{'type':'record','fields':[{'name':'d1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'E','type':{'type':'record','fields':[{'name':'F','type':{'type':'record','fields':["
                + "{'name':'f1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''}]},'doc':'','default':''}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
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
        IndexedRecord inputRecord = GenericDataRecordHelper.createRecord(inputAsObject);

        List<String> keyList = Arrays.asList("a1", "B.C.D.d1", "B.C.c1", "B.E", "G.H.h1", "a2", "I.i1");

        String keyOutput = ("{'type':'record','name':'InRecord','fields':[" + "{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':["
                + "{'name':'D','type':{'type':'record','name':'DRecord','fields':[{'name':'d1','type':'string'}]},'doc':'','default':''},"
                + "{'name':'c1','type':'string'}]},'doc':'','default':''},"
                + "{'name':'E','type':{'type':'record','name':'ERecord','fields':["
                + "{'name':'F','type':{'type':'record','name':'FRecord','fields':[{'name':'f1','type':'string'}]}},"
                + "{'name':'e1','type':'string'},{'name':'e2','type':'string'}]}}]},'doc':'','default':''},"
                + "{'name':'G','type':{'type':'record','name':'GRecord','fields':["
                + "{'name':'H','type':{'type':'record','name':'HRecord','fields':[{'name':'h1','type':'string'}]},'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'a2','type':['null','string'],'doc':'','default':''},"
                + "{'name':'I','type':{'type':'record','fields':[{'name':'i1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(keyOutput, SchemaGeneratorUtils.extractKeys(inputRecord.getSchema(), keyList).toString());

        String valueOutput = ("{'type':'record','name':'value_InRecord','fields':[{'name':'B','type':{'type':'record','name':'value_BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'value_CRecord','fields':["
                + "{'name':'c2','type':'string'}]},'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'G','type':{'type':'record','name':'value_GRecord','fields':["
                + "{'name':'H','type':{'type':'record','name':'value_HRecord','fields':["
                + "{'name':'I','type':{'type':'record','name':'value_IRecord','fields':[{'name':'i1','type':'string'}]},"
                + "'doc':'','default':''}]},'doc':'','default':''}]}," + "'doc':'','default':''}]}").replaceAll("\\'", "\"");
        assertEquals(valueOutput, SchemaGeneratorUtils.extractValues(inputRecord.getSchema(), keyList).toString());

        Schema kvSchema = SchemaGeneratorUtils.extractKeyValues(inputRecord.getSchema(), keyList);
        assertEquals(generateKVOutput(keyOutput, valueOutput), kvSchema.toString());

        String mergedSchema = ("{'type':'record','name':'InRecord','fields':[{'name':'a1','type':'string'},"
                + "{'name':'B','type':{'type':'record','name':'BRecord','fields':["
                + "{'name':'C','type':{'type':'record','name':'CRecord','fields':["
                + "{'name':'D','type':{'type':'record','name':'DRecord','fields':["
                + "{'name':'d1','type':'string'}]},'doc':'','default':''},{'name':'c1','type':'string'},"
                + "{'name':'c2','type':'string'}]},'doc':'','default':''},"
                + "{'name':'E','type':{'type':'record','name':'ERecord','fields':["
                + "{'name':'F','type':{'type':'record','name':'FRecord','fields':["
                + "{'name':'f1','type':'string'}]}},{'name':'e1','type':'string'},{'name':'e2','type':'string'}]}}]},'doc':'','default':''},"
                + "{'name':'G','type':{'type':'record','name':'GRecord','fields':["
                + "{'name':'H','type':{'type':'record','name':'HRecord','fields':["
                + "{'name':'h1','type':'string'},{'name':'I','type':{'type':'record','name':'value_IRecord','fields':["
                + "{'name':'i1','type':'string'}]},'doc':'','default':''}]},'doc':'','default':''}]},'doc':'','default':''},"
                + "{'name':'a2','type':['null','string'],'doc':'','default':''},"
                + "{'name':'I','type':{'type':'record','fields':[{'name':'i1','type':['null','string'],'doc':'','default':''}]},'doc':'','default':''}]}")
                        .replaceAll("\\'", "\"");
        assertEquals(mergedSchema, SchemaGeneratorUtils.mergeKeyValues(kvSchema).toString());
    }

    private String generateKVOutput(String keyOutput, String valueOutput) {
        return ("{'type':'record','name':'keyvalue','fields':[{'name':'key','type':" + keyOutput
                + ",'doc':'','default':''},"
                + "{'name':'value','type':" + valueOutput + ",'doc':'','default':''}]}") //
                        .replaceAll("\\'", "\"");
    }
}
