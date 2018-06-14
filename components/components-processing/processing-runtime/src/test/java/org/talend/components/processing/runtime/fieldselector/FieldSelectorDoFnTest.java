// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.processing.runtime.fieldselector;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.talend.components.processing.runtime.SampleAvpathSchemas.SyntheticDatasets.getSubrecords;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.processing.definition.fieldselector.FieldSelectorProperties;
import org.talend.components.processing.definition.fieldselector.SelectorProperties;
import org.talend.components.processing.runtime.SampleAvpathSchemas;
import org.talend.daikon.exception.TalendRuntimeException;

public class FieldSelectorDoFnTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final Schema inputSimpleSchema = SchemaBuilder
            .record("inputRow") //
            .fields() //
            .optionalString("a")
            .optionalString("b")
            .optionalString("c")
            .endRecord();

    private final GenericRecord inputSimpleRecord = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", "aaa") //
            .set("b", "BBB") //
            .set("c", "Ccc") //
            .build();

    private final GenericRecord inputRecordWithEmptyValue = new GenericRecordBuilder(inputSimpleSchema) //
            .set("a", "aaa") //
            .set("b", "BBB") //
            .build();

    private final IndexedRecord inputHierarchical = SampleAvpathSchemas.SyntheticDatasets.getRandomRecord(new Random(0),
            SampleAvpathSchemas.SyntheticDatasets.RECORD_A);

    private final IndexedRecord inputNullableHierarchical1 = SampleAvpathSchemas.SyntheticDatasets
            .getRandomRecord(new Random(0), SampleAvpathSchemas.NullableSyntheticDatasets.RECORD_A);

    private final IndexedRecord inputNullableHierarchical2 = SampleAvpathSchemas.SyntheticDatasets
            .getRandomRecord(new Random(1), SampleAvpathSchemas.NullableSyntheticDatasets.RECORD_A);

    private final IndexedRecord inputNullableHierarchicalWithLoops1 = SampleAvpathSchemas.SyntheticDatasets
            .getRandomRecord(new Random(0), SampleAvpathSchemas.NullableSyntheticDatasets.RECORD_B);

    private final IndexedRecord inputNullableHierarchicalWithLoops2 = SampleAvpathSchemas.SyntheticDatasets
            .getRandomRecord(new Random(1), SampleAvpathSchemas.NullableSyntheticDatasets.RECORD_B);

    private final IndexedRecord[] inputB = SampleAvpathSchemas.SyntheticDatasets.getRandomRecords(1000, new Random(0),
            SampleAvpathSchemas.SyntheticDatasets.RECORD_B);

    protected static FieldSelectorProperties addSelector(FieldSelectorProperties fsp, String field, String path) {
        // Create a new properties if one wasn't passed in.
        if (fsp == null) {
            fsp = new FieldSelectorProperties("test");
            fsp.init();
            // Remove the default critera.
            fsp.selectors.subProperties.clear();
        }

        // Create and add a new selector with the requested properties.
        SelectorProperties selector = new SelectorProperties("filter");
        selector.init();
        fsp.selectors.addRow(selector);

        if (field != null)
            selector.field.setValue(field);
        if (path != null)
            selector.path.setValue(path);

        return fsp;
    }

    /**
     * When there are no user input, the component not return any data
     */
    @Test
    public void noSelectorForField() throws Exception {
        // Create a filter row with exactly one criteria that hasn't been filled by the user.
        FieldSelectorProperties properties = addSelector(null, "test", null);
        assertThat(properties.selectors.getPropertiesList(), hasSize(1));

        SelectorProperties selector = properties.selectors.getPropertiesList().iterator().next();
        assertThat(selector.field.getStringValue(), is("test"));
        assertThat(selector.path.getStringValue(), is(""));

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);

        //
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
    }

    /**
     * When there are no user input, the component not return any data
     */
    @Test
    public void noSelectorForPath() throws Exception {
        // Create a filter row with exactly one criteria that hasn't been filled by the user.
        FieldSelectorProperties properties = addSelector(null, null, ".test");
        assertThat(properties.selectors.getPropertiesList(), hasSize(1));

        SelectorProperties selector = properties.selectors.getPropertiesList().iterator().next();
        assertThat(selector.field.getStringValue(), is(""));
        assertThat(selector.path.getStringValue(), is(".test"));

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);

        //
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(0, outputs.size());
    }

    @Test
    public void selectSimpleElement() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "aOutput", "a");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);
        assertEquals(1, outputs.size());
        List<Field> fields = outputs.get(0).getSchema().getFields();
        assertEquals(1, fields.size());
        assertEquals("aOutput", fields.get(0).name());
        assertEquals("aaa", outputs.get(0).get(0));
    }

    @Test
    public void selectSimpleElements() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "aOutput", "a");
        properties = addSelector(properties, "cOutput", "c");
        properties = addSelector(properties, "aSecondOutput", "a");
        properties = addSelector(properties, "bOutput", "b");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord);

        assertEquals(1, outputs.size());
        List<Field> fields = outputs.get(0).getSchema().getFields();
        assertEquals(4, fields.size());
        assertEquals("aOutput", fields.get(0).name());
        assertEquals("aaa", outputs.get(0).get(0));
        assertEquals("cOutput", fields.get(1).name());
        assertEquals("Ccc", outputs.get(0).get(1));
        assertEquals("aSecondOutput", fields.get(2).name());
        assertEquals("aaa", outputs.get(0).get(2));
        assertEquals("bOutput", fields.get(3).name());
        assertEquals("BBB", outputs.get(0).get(3));
    }

    @Test
    public void selectSimpleElementsMultiplestime() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "aOutput", "a");
        properties = addSelector(properties, "cOutput", "c");
        properties = addSelector(properties, "aSecondOutput", "a");
        properties = addSelector(properties, "bOutput", "b");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(inputSimpleRecord, inputSimpleRecord);

        assertEquals(2, outputs.size());
        List<Field> fields = outputs.get(0).getSchema().getFields();
        assertEquals(4, fields.size());
        assertEquals("aOutput", fields.get(0).name());
        assertEquals("aaa", outputs.get(0).get(0));
        assertEquals("cOutput", fields.get(1).name());
        assertEquals("Ccc", outputs.get(0).get(1));
        assertEquals("aSecondOutput", fields.get(2).name());
        assertEquals("aaa", outputs.get(0).get(2));
        assertEquals("bOutput", fields.get(3).name());
        assertEquals("BBB", outputs.get(0).get(3));
        fields = outputs.get(1).getSchema().getFields();
        assertEquals(4, fields.size());
        assertEquals("aOutput", fields.get(0).name());
        assertEquals("aaa", outputs.get(1).get(0));
        assertEquals("cOutput", fields.get(1).name());
        assertEquals("Ccc", outputs.get(1).get(1));
        assertEquals("aSecondOutput", fields.get(2).name());
        assertEquals("aaa", outputs.get(1).get(2));
        assertEquals("bOutput", fields.get(3).name());
        assertEquals("BBB", outputs.get(1).get(3));
    }

    @Test
    public void selectSimpleElementsWithEmptyValues() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "aOutput", "a");
        properties = addSelector(properties, "cOutput", "c");
        properties = addSelector(properties, "aSecondOutput", "a");
        properties = addSelector(properties, "bOutput", "b");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(inputRecordWithEmptyValue);

        assertEquals(1, outputs.size());
        List<Field> fields = outputs.get(0).getSchema().getFields();
        assertEquals(4, fields.size());
        assertEquals("aOutput", fields.get(0).name());
        assertEquals("aaa", outputs.get(0).get(0));
        assertEquals("cOutput", fields.get(1).name());
        assertNull(outputs.get(0).get(1));
        assertEquals("aSecondOutput", fields.get(2).name());
        assertEquals("aaa", outputs.get(0).get(2));
        assertEquals("bOutput", fields.get(3).name());
        assertEquals("BBB", outputs.get(0).get(3));
    }


    @Test(expected = TalendRuntimeException.class)
    public void selectInvalidElements() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "a b", "a");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        fnTester.processBundle(inputSimpleRecord);
        // throw exception
    }


    @Test
    public void testBasicHierarchical() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "id", ".id");
        properties = addSelector(properties, "name", ".a1.name");
        properties = addSelector(properties, "subname", ".a1.a2.name");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(inputHierarchical);

        assertEquals(1, outputs.size());
        List<Field> fields = outputs.get(0).getSchema().getFields();
        assertEquals(3, fields.size());
        assertEquals("id", fields.get(0).name());
        assertEquals(1, outputs.get(0).get(0));
        assertEquals("name", fields.get(1).name());
        assertEquals("P8A933FLOC", outputs.get(0).get(1));
        assertEquals("subname", fields.get(2).name());
        assertEquals("Q2G5V64PQQ", outputs.get(0).get(2));
    }

    @Test
    public void testHierarchicalWithSelector() throws Exception {
        FieldSelectorProperties properties =
                addSelector(null, "yearOfToyota", ".automobiles{.maker === \"Toyota\"}.year");
        IndexedRecord input = SampleAvpathSchemas.Vehicles.getDefaultVehicleCollection();

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(input);

        assertEquals(1, outputs.size());
        List<Field> fields = outputs.get(0).getSchema().getFields();
        assertEquals(1, fields.size());
        assertEquals("yearOfToyota", fields.get(0).name());
        assertThat(((List<Integer>) outputs.get(0).get(0)), hasItems(2016, 2017));
    }

    @Test(expected = TalendRuntimeException.class)
    public void testHierarchicalUnknownColumn() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "id", ".unknow");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);

        List<IndexedRecord> outputs = fnTester.processBundle(inputHierarchical);

        // None of the records can possibly match. This is an invalid schema, not an empty field
        // TODO(TFD-2194): This should throw an exception if possible.
        // Until that is the case, there should be no output
        assertEquals(0, outputs.size());
    }

    @Test(expected = TalendRuntimeException.class)
    public void testHierarchicalSyntaxError() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "id", "asdf&*{.\\\\t");

        FieldSelectorDoFn function = new FieldSelectorDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
        List<IndexedRecord> outputs = fnTester.processBundle(inputHierarchical);
    }

    @Test
    public void testHierarchicalWithPredicateOnRoot() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "idoutput", ".{.id == 1}.id");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputB);
        for (IndexedRecord main : output) {
            List<Field> fields = main.getSchema().getFields();
            assertEquals(1, fields.size());
            assertEquals("idoutput", fields.get(0).name());
            Integer element = ((ArrayList<Integer>)main.get(0)).get(0);
            assertEquals(1, element.intValue());
        }
    }

    @Test
    public void testHierarchicalSubRecordHasValueGt10() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "value", ".b1{.value > 10}");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputB);
        for (IndexedRecord main : output) {
            List<Field> fields = main.getSchema().getFields();
            assertEquals(1, fields.size());
            assertEquals("value", fields.get(0).name());
            for (IndexedRecord element : (List<IndexedRecord>) main.get(0)) {
                List<Field> subFields = element.getSchema().getFields();
                assertEquals("id", subFields.get(0).name());
                assertEquals("name", subFields.get(1).name());
                assertEquals("value", subFields.get(2).name());
                assertEquals("b2", subFields.get(3).name());
                assertThat((Double) element.get(2), greaterThan(10d));
            }
        }
    }

    @Test
    public void testHierarchicalAllSubRecordsHaveValueGt10() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "value", ".b1{.value <= 10}");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputB);
        for (IndexedRecord main : output) {
            List<Field> fields = main.getSchema().getFields();
            assertEquals(1, fields.size());
            assertEquals("value", fields.get(0).name());
            for (IndexedRecord element : (List<IndexedRecord>) main.get(0)) {
                assertThat((Integer) element.get(0), lessThanOrEqualTo(10));
            }
        }
    }

    @Test
    public void testHierarchicalFirstRecordValue() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "value", ".b1[0].value");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputB);
        for (IndexedRecord main : output) {
            List<Field> fields = main.getSchema().getFields();
            assertEquals(1, fields.size());
            assertEquals("value", fields.get(0).name());
        }
    }

    @Test
    public void testHierarchicalLastRecordValue() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "value", ".b1[-1].value");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputB);
        for (IndexedRecord main : output) {
            List<Field> fields = main.getSchema().getFields();
            assertEquals(1, fields.size());
            assertEquals("value", fields.get(0).name());
        }
    }

    @Test
    public void testHierarchicalSubRecordsWithId1Or2HasValueGt10_Alternative() throws Exception {
        FieldSelectorProperties properties =
                addSelector(null, "expectedb1", ".b1{.id == 1 && .value > 10 || .id == 2 && .value > 10}");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputB);
        for (IndexedRecord main : output) {
            boolean atLeastOne = false;
            for (IndexedRecord subrecord : getSubrecords(main)) {
                int id = (int) subrecord.get(0);
                if ((double) subrecord.get(2) > 10 && (id == 1 || id == 2))
                    atLeastOne = true;
            }
            if (atLeastOne) {
                List<Field> fields = main.getSchema().getFields();
                assertEquals(1, fields.size());
                assertEquals("expectedb1", fields.get(0).name());
                List<IndexedRecord> subElements = (List<IndexedRecord>) main.get(0);
                for (IndexedRecord subElement : subElements) {
                    List<Field> subFields = subElement.getSchema().getFields();
                    assertEquals(4, subFields.size());
                    assertEquals("id", subFields.get(0).name());
                    assertEquals("name", subFields.get(1).name());
                    assertEquals("value", subFields.get(2).name());
                    assertEquals("b2", subFields.get(3).name());

                    assertThat((Integer) subElement.get(0), isOneOf(1, 2));
                    assertThat((Double) subElement.get(2), greaterThan(10d));
                }
            } else {
                assertFalse("You whould not create elements when there is not items", true);
            }
        }
    }

    @Test
    public void testHierarchicalSubrecordWithSubSubRecordValueGt10() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "expectedb1", ".b1{.b2.value > 10}");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputB);

        for (IndexedRecord main : output) {
            boolean atLeastOne = false;
            for (IndexedRecord subrecord : getSubrecords(main)) {
                for (IndexedRecord subsubrecord : getSubrecords(subrecord)) {
                    if ((double) subsubrecord.get(2) > 10)
                        atLeastOne = true;
                }
            }
            if (atLeastOne) {
                List<Field> fields = main.getSchema().getFields();
                assertEquals(1, fields.size());
                assertEquals("expectedb1", fields.get(0).name());
                List<IndexedRecord> subElements = (List<IndexedRecord>) main.get(0);
                for (IndexedRecord subElement : subElements) {
                    List<Field> subFields = subElement.getSchema().getFields();
                    assertEquals(4, subFields.size());
                    assertEquals("id", subFields.get(0).name());
                    assertEquals("name", subFields.get(1).name());
                    assertEquals("value", subFields.get(2).name());
                    assertEquals("b2", subFields.get(3).name());

                    IndexedRecord subSubElement = (IndexedRecord) subElement.get(3);
                    List<Field> subSubFields = subSubElement.getSchema().getFields();
                    assertEquals(3, subSubFields.size());
                    assertEquals("id", subSubFields.get(0).name());
                    assertEquals("name", subSubFields.get(1).name());
                    assertEquals("value", subSubFields.get(2).name());

                    assertThat((Double) subSubElement.get(2), greaterThan(10d));
                }
            } else {
                assertFalse("You whould not create elements when there is not items", true);
            }
        }
    }

    /**
     * This test will select values on inputHierarchical that are not present on inputHierarchical2 and vice versa.
     * The goal is to see if the if the schema of the output is stable between the two output
     * 
     * @throws Exception
     */
    @Test
    public void selectHierarchicalValues() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "outputsimple", ".id");
        properties = addSelector(properties, "input1level1", ".a1{.id == 1}");
        properties = addSelector(properties, "input1level2", ".a1{.a2.id == 5}");
        properties = addSelector(properties, "input2level1", ".a1{.id == 3}");
        properties = addSelector(properties, "input2level2", ".a1{.a2.id == 3}");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output = fnTester.processBundle(inputNullableHierarchical1, inputNullableHierarchical2);

        IndexedRecord output1 = output.get(0);
        List<Field> fields = output1.getSchema().getFields();
        assertEquals(5, fields.size());
        assertEquals("outputsimple", fields.get(0).name());
        assertThat((Integer) output1.get(0), is(1));

        assertEquals("input1level1", fields.get(1).name());
        IndexedRecord output1level1 = ((List<IndexedRecord>) output1.get(1)).get(0);
        assertThat((Integer) output1level1.get(0), is(1));
        IndexedRecord subOutput1level1 = (IndexedRecord) output1level1.get(3);
        assertThat((Integer) subOutput1level1.get(0), is(5));

        assertEquals("input1level2", fields.get(2).name());
        IndexedRecord output1level2 = ((List<IndexedRecord>) output1.get(2)).get(0);
        assertThat((Integer) output1level2.get(0), is(1));
        IndexedRecord subOutput1level2 = (IndexedRecord) output1level2.get(3);
        assertThat((Integer) subOutput1level2.get(0), is(5));

        assertEquals("input2level1", fields.get(3).name());
        assertThat((List<IndexedRecord>) output1.get(3), is(empty()));
        assertEquals("input2level2", fields.get(4).name());
        assertThat((List<IndexedRecord>) output1.get(4), is(empty()));

        IndexedRecord output2 = output.get(1);
        fields = output2.getSchema().getFields();
        assertEquals(5, fields.size());
        assertEquals("outputsimple", fields.get(0).name());
        assertThat((Integer) output2.get(0), is(6));
        assertEquals("input1level1", fields.get(1).name());
        assertThat((List<IndexedRecord>) output2.get(1), is(empty()));
        assertEquals("input1level2", fields.get(2).name());
        assertThat((List<IndexedRecord>) output2.get(2), is(empty()));

        assertEquals("input2level1", fields.get(3).name());
        IndexedRecord output2level1 = ((List<IndexedRecord>) output2.get(3)).get(0);
        assertThat((Integer) output2level1.get(0), is(3));
        IndexedRecord subOutput2level1 = (IndexedRecord) output2level1.get(3);
        assertThat((Integer) subOutput2level1.get(0), is(3));

        assertEquals("input2level2", fields.get(4).name());
        IndexedRecord output2level2 = ((List<IndexedRecord>) output2.get(4)).get(0);
        assertThat((Integer) output2level2.get(0), is(3));
        IndexedRecord subOutput2level2 = (IndexedRecord) output2level2.get(3);
        assertThat((Integer) subOutput2level2.get(0), is(3));
    }

    /**
     * This test will select values on inputHierarchical that are not present on inputHierarchical2 and vice versa.
     * The goal is to see if the if the schema of the output is stable between the two output
     * 
     * @throws Exception
     */
    @Test
    public void selectHierarchicalValuesWithLoops() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "outputsimple", ".id");
        properties = addSelector(properties, "input1level1", ".b1{.id == 4}");
        properties = addSelector(properties, "input1level2", ".b1{.b2.id == 7}");
        properties = addSelector(properties, "input2level1", ".b1{.id == 6}");
        properties = addSelector(properties, "input2level2", ".b1{.b2.id == 6}");
        properties = addSelector(properties, "input2list", ".b1[2]{.b2.id == 6}");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output =
                fnTester.processBundle(inputNullableHierarchicalWithLoops1, inputNullableHierarchicalWithLoops2);

        IndexedRecord output1 = output.get(0);
        List<Field> fields = output1.getSchema().getFields();
        assertEquals(6, fields.size());
        assertEquals("outputsimple", fields.get(0).name());
        assertThat((Integer) output1.get(0), is(1));

        assertEquals("input1level1", fields.get(1).name());
        IndexedRecord output1level1 = ((List<IndexedRecord>) output1.get(1)).get(0);
        assertThat((Integer) output1level1.get(0), is(4));
        IndexedRecord subOutput1level1 = (IndexedRecord) output1level1.get(3);
        assertThat((Integer) subOutput1level1.get(0), is(7));

        assertEquals("input1level2", fields.get(2).name());
        IndexedRecord output1level2 = ((List<IndexedRecord>) output1.get(2)).get(0);
        assertThat((Integer) output1level2.get(0), is(4));
        IndexedRecord subOutput1level2 = (IndexedRecord) output1level2.get(3);
        assertThat((Integer) subOutput1level2.get(0), is(7));

        assertEquals("input2level1", fields.get(3).name());
        assertThat((List<IndexedRecord>) output1.get(3), is(empty()));
        assertEquals("input2level2", fields.get(4).name());
        assertThat((List<IndexedRecord>) output1.get(4), is(empty()));
        assertEquals("input2list", fields.get(5).name());
        assertThat((List<IndexedRecord>) output1.get(5), is(empty()));

        IndexedRecord output2 = output.get(1);
        fields = output2.getSchema().getFields();
        assertEquals(6, fields.size());
        assertEquals("outputsimple", fields.get(0).name());
        assertThat((Integer) output2.get(0), is(6));
        assertEquals("input1level1", fields.get(1).name());
        assertThat((List<IndexedRecord>) output2.get(1), is(empty()));
        assertEquals("input1level2", fields.get(2).name());
        assertThat((List<IndexedRecord>) output2.get(2), is(empty()));

        assertEquals("input2level1", fields.get(3).name());
        IndexedRecord output2level1 = ((List<IndexedRecord>) output2.get(3)).get(0);
        assertThat((Integer) output2level1.get(0), is(6));
        IndexedRecord subOutput2level1 = (IndexedRecord) output2level1.get(3);
        assertThat((Integer) subOutput2level1.get(0), is(4));

        assertEquals("input2level2", fields.get(4).name());
        IndexedRecord output2level2 = ((List<IndexedRecord>) output2.get(4)).get(0);
        assertThat((Integer) output2level2.get(0), is(2));
        IndexedRecord subOutput2level2 = (IndexedRecord) output2level2.get(3);
        assertThat((Integer) subOutput2level2.get(0), is(6));

        assertEquals("input2list", fields.get(5).name());
        IndexedRecord output2list = ((List<IndexedRecord>) output2.get(4)).get(0);
        assertThat((Integer) output2list.get(0), is(2));
        IndexedRecord subOutput2list = (IndexedRecord) output2list.get(3);
        assertThat((Integer) subOutput2list.get(0), is(6));
    }

    /**
     * Retrieve the third element inside a loop. Since the first record have only two elements on its loops, it's going
     * to retrieve the last element.
     */
    @Test
    public void selectHierarchicalValuesGetOneElementFromLoops() throws Exception {
        FieldSelectorProperties properties = addSelector(null, "subElementFromList", ".b1[2].id");
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of( //
                new FieldSelectorDoFn().withProperties(properties));

        List<IndexedRecord> output =
                fnTester.processBundle(inputNullableHierarchicalWithLoops1, inputNullableHierarchicalWithLoops2);

        IndexedRecord output1 = output.get(0);
        List<Field> fields = output1.getSchema().getFields();
        assertEquals(1, fields.size());
        assertEquals("subElementFromList", fields.get(0).name());
        assertThat((Integer) output1.get(0), is(8));

        IndexedRecord output2 = output.get(1);
        fields = output2.getSchema().getFields();
        assertEquals(1, fields.size());
        assertEquals("subElementFromList", fields.get(0).name());
        assertThat((Integer) output2.get(0), is(2));
    }
}
