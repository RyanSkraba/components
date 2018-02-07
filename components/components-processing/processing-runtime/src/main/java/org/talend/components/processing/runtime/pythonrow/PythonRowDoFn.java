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

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.pythonrow.MapType;
import org.talend.components.processing.definition.pythonrow.PythonRowProperties;
import org.talend.daikon.avro.converter.JsonGenericRecordConverter;
import org.talend.daikon.avro.inferrer.JsonSchemaInferrer;
import org.talend.daikon.properties.ValidationResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PythonRowDoFn extends DoFn<IndexedRecord, IndexedRecord> implements RuntimableRuntime<PythonRowProperties> {

    private PythonRowProperties properties = null;

    private PythonInterpreter interpreter = null;

    private PyObject pyFn = null;

    private JsonGenericRecordConverter jsonGenericRecordConverter = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PythonRowProperties componentProperties) {
        this.properties = (PythonRowProperties) componentProperties;
        return ValidationResult.OK;
    }

    @Setup
    public void setup() throws Exception {
        interpreter = new PythonInterpreter();
        if (MapType.MAP.equals(properties.mapType.getValue())) {
            interpreter.exec(setUpMap());
        } else { // flatmap
            interpreter.exec(setUpFlatMap());
        }
        pyFn = interpreter.get("userFunction");
    }

    @ProcessElement
    public void processElement(ProcessContext context) throws IOException {
        if (context.element() != null) {
            if (MapType.MAP.equals(properties.mapType.getValue())) {
                map(context.element(), context);
            } else { // flatmap
                flatMap(context.element(), context);
            }
        }
    }

    private String setUpMap() {
        return "import json\n" //
                + "import collections\n" //
                + "def userFunction(inputJSON):\n" //
                + "    input = json.loads(inputJSON, object_pairs_hook=collections.OrderedDict)\n" //
                + "    output = json.loads(\"{}\", object_pairs_hook=collections.OrderedDict)\n" //
                + "    " + properties.pythonCode.getValue().replaceAll("\n", "\n    ").trim() //
                + "\n    return json.dumps(output)";
    }

    private String setUpFlatMap() {
        return "import json\n" //
                + "import collections\n" //
                + "def userFunction(inputJSON):\n" //
                + "    input = json.loads(inputJSON, object_pairs_hook=collections.OrderedDict)\n" //
                + "    outputList = []\n" //
                + "    " + properties.pythonCode.getValue().replaceAll("\n", "\n    ").trim() //
                + "\n    return [ json.dumps(outputElement) for outputElement in outputList ]";

    }

    private void map(IndexedRecord input, ProcessContext context) throws IOException {
        PyObject output = pyFn.__call__(new PyString(input.toString()));

        if (jsonGenericRecordConverter == null) {
            JsonSchemaInferrer jsonSchemaInferrer = new JsonSchemaInferrer(new ObjectMapper());
            Schema jsonSchema = jsonSchemaInferrer.inferSchema(output.toString());
            jsonGenericRecordConverter = new JsonGenericRecordConverter(jsonSchema);
        }

        GenericRecord outputRecord = jsonGenericRecordConverter.convertToAvro(output.toString());
        context.output(outputRecord);
    }

    private void flatMap(IndexedRecord input, ProcessContext context) throws IOException {
        // Prepare Python environment
        PyObject outputList = pyFn.__call__(new PyString(input.toString()));

        if (outputList instanceof PyList) {
            PyList list = (PyList) outputList;
            for (Object output : list) {
                if (jsonGenericRecordConverter == null) {
                    JsonSchemaInferrer jsonSchemaInferrer = new JsonSchemaInferrer(new ObjectMapper());
                    Schema jsonSchema = jsonSchemaInferrer.inferSchema(output.toString());
                    jsonGenericRecordConverter = new JsonGenericRecordConverter(jsonSchema);
                }
                GenericRecord outputRecord = jsonGenericRecordConverter.convertToAvro(output.toString());
                context.output(outputRecord);
            }
        }
    }

    @Teardown
    public void tearDown() {
        interpreter.close();
    }
}
