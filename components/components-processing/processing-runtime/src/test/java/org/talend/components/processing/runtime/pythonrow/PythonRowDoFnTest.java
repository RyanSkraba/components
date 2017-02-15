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
package org.talend.components.processing.runtime.pythonrow;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.processing.pythonrow.MapType;
import org.talend.components.processing.pythonrow.PythonRowProperties;
import org.talend.daikon.avro.GenericDataRecordHelper;
import org.talend.daikon.properties.ValidationResult;

public class PythonRowDoFnTest {

    private static IndexedRecord inputIndexedRecord = null;

    private static IndexedRecord outputIndexedRecord = null;

    @BeforeClass
    public static void setUp() throws IOException {
        Object[] inputAsObject1 = new Object[] { "rootdata",
                new Object[] { "subdata", new Object[] { "subsubdata1", 28, 42l }, "subdata2" } };
        Schema inputSchema = GenericDataRecordHelper.createSchemaFromObject("MyRecord", inputAsObject1);
        inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject1);

        Object[] inputAsObject2 = new Object[] { "rootdata2",
                new Object[] { "subdatabefore", new Object[] { "subsubdatabefore", 33, 55l }, "subdataend" } };
        outputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject2);
    }

    @Test
    public void test_NullInput() throws Exception {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        properties.pythonCode.setValue("outputList.append(input)");
        PythonRowDoFn function = new PythonRowDoFn();
        assertEquals(ValidationResult.OK, function.initialize(null, properties));
        DoFnTester<Object, Object> fnTester = DoFnTester.of(function);
        List<Object> outputs = fnTester.processBundle((IndexedRecord) null);
        assertEquals(0, outputs.size());
    }

    @Test
    public void test_Map_doNothing() throws Exception {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        properties.pythonCode.setValue("output = input");
        PythonRowDoFn function = new PythonRowDoFn();
        assertEquals(ValidationResult.OK, function.initialize(null, properties));
        DoFnTester<Object, Object> fnTester = DoFnTester.of(function);
        List<Object> outputs = fnTester.processBundle(inputIndexedRecord);
        assertEquals(1, outputs.size());
        assertEquals(inputIndexedRecord.toString(), outputs.get(0).toString());
    }

    @Test
    public void test_Map_ApplyATransformation() throws Exception {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();

        StringBuilder sb = new StringBuilder();
        sb.append("output = input\n");
        sb.append("output['a1'] = \"rootdata2\"\n");
        sb.append("output['B']['b1'] = \"subdatabefore\"\n");
        sb.append("output['B']['C']['c1'] = \"subsubdatabefore\"\n");
        sb.append("output['B']['C']['c2'] = 33\n");
        sb.append("output['B']['C']['c3'] = 55l\n");
        sb.append("output['B']['b2'] = \"subdataend\"\n");
        properties.pythonCode.setValue(sb.toString());
        PythonRowDoFn function = new PythonRowDoFn();
        assertEquals(ValidationResult.OK, function.initialize(null, properties));
        DoFnTester<Object, Object> fnTester = DoFnTester.of(function);
        List<Object> outputs = fnTester.processBundle(inputIndexedRecord);
        assertEquals(1, outputs.size());
        assertEquals(outputIndexedRecord.toString(), outputs.get(0).toString());
    }

    @Test
    public void test_FlatMap_doNothing() throws Exception {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        properties.mapType.setValue(MapType.FLATMAP);
        properties.pythonCode.setValue("outputList.append(input)");
        PythonRowDoFn function = new PythonRowDoFn();
        assertEquals(ValidationResult.OK, function.initialize(null, properties));
        DoFnTester<Object, Object> fnTester = DoFnTester.of(function);
        List<Object> outputs = fnTester.processBundle(inputIndexedRecord);
        assertEquals(1, outputs.size());
        assertEquals(inputIndexedRecord.toString(), outputs.get(0).toString());
    }

    @Test
    public void test_FlatMap_DupplicateInput() throws Exception {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        properties.mapType.setValue(MapType.FLATMAP);
        StringBuilder sb = new StringBuilder();
        sb.append("import copy\n");
        sb.append("outputList.append(input)\n");
        sb.append("outputList.append(input)\n");
        sb.append("outputList.append(copy.deepcopy(input))\n");
        properties.pythonCode.setValue(sb.toString());
        PythonRowDoFn function = new PythonRowDoFn();
        assertEquals(ValidationResult.OK, function.initialize(null, properties));
        DoFnTester<Object, Object> fnTester = DoFnTester.of(function);
        List<Object> outputs = fnTester.processBundle(inputIndexedRecord);
        assertEquals(3, outputs.size());
        assertEquals(inputIndexedRecord.toString(), outputs.get(0).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(1).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(2).toString());
    }

    @Test
    public void test_FlatMap_MultipleInputs() throws Exception {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        properties.mapType.setValue(MapType.FLATMAP);
        StringBuilder sb = new StringBuilder();
        sb.append("import copy\n");
        sb.append("outputList.append(input)\n");
        sb.append("outputList.append(input)\n");
        sb.append("outputList.append(copy.deepcopy(input))\n");
        properties.pythonCode.setValue(sb.toString());
        PythonRowDoFn function = new PythonRowDoFn();
        assertEquals(ValidationResult.OK, function.initialize(null, properties));
        DoFnTester<Object, Object> fnTester = DoFnTester.of(function);
        List<Object> outputs = fnTester.processBundle(inputIndexedRecord, inputIndexedRecord, inputIndexedRecord);
        assertEquals(9, outputs.size());
        assertEquals(inputIndexedRecord.toString(), outputs.get(0).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(1).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(2).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(3).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(4).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(5).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(6).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(7).toString());
        assertEquals(inputIndexedRecord.toString(), outputs.get(8).toString());
    }

    @Test
    public void test_FlatMap_DupplicateOutputAndApplyATransformation() throws Exception {

        PythonRowProperties properties = new PythonRowProperties("test");
        properties.init();
        properties.mapType.setValue(MapType.FLATMAP);

        StringBuilder sb = new StringBuilder();
        sb.append("import copy\n");
        sb.append("outputList.append(copy.deepcopy(input))\n");
        sb.append("outputList.append(input)\n"); // will be converted to inputIndexedRecord2
        sb.append("output = input\n");
        sb.append("output['a1'] = \"rootdata2\"\n");
        sb.append("output['B']['b1'] = \"subdatabefore\"\n");
        sb.append("output['B']['C']['c1'] = \"subsubdatabefore\"\n");
        sb.append("output['B']['C']['c2'] = 33\n");
        sb.append("output['B']['C']['c3'] = 55l\n");
        sb.append("output['B']['b2'] = \"subdataend\"\n");
        sb.append("outputList.append(output)\n");
        properties.pythonCode.setValue(sb.toString());
        PythonRowDoFn function = new PythonRowDoFn();
        assertEquals(ValidationResult.OK, function.initialize(null, properties));
        DoFnTester<Object, Object> fnTester = DoFnTester.of(function);
        List<Object> outputs = fnTester.processBundle(inputIndexedRecord);
        assertEquals(3, outputs.size());
        assertEquals(inputIndexedRecord.toString(), outputs.get(0).toString());
        assertEquals(outputIndexedRecord.toString(), outputs.get(1).toString());
        assertEquals(outputIndexedRecord.toString(), outputs.get(2).toString());
    }
}