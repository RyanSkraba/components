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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
import org.talend.components.api.exception.ComponentException;
import org.talend.components.processing.definition.pythonrow.MapType;
import org.talend.components.processing.definition.pythonrow.PythonRowProperties;
import org.talend.daikon.avro.converter.JsonGenericRecordConverter;
import org.talend.daikon.avro.inferrer.JsonSchemaInferrer;
import org.talend.daikon.properties.ValidationResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PythonRowDoFn extends DoFn<IndexedRecord, IndexedRecord>
        implements RuntimableRuntime<PythonRowProperties> {

    private PythonRowProperties properties = null;

    private PythonInterpreter interpreter = null;

    private PyObject pyFn = null;

    private JsonGenericRecordConverter jsonGenericRecordConverter = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PythonRowProperties componentProperties) {
        this.properties = componentProperties;
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

    private String securityPyFunc() {
        String pySript;
        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("import_restriction.py");
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = resourceAsStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            pySript = result.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new ComponentException(e);
        }
        return pySript;
    }

    private String setUpMap() {
        return securityPyFunc() + "import json\n" //
                + "import collections\n" //
                + "def userFunction(inputJSON):\n" //
                + "  input = json.loads(inputJSON, object_pairs_hook=collections.OrderedDict)\n" //
                + "  output = json.loads(\"{}\", object_pairs_hook=collections.OrderedDict)\n" //
                + "  try:\n" //
                + "    emptyFunction()\n" //
                + "    " + properties.pythonCode.getValue().replaceAll("\n", "\n    ").trim() + "\n"//
                + "  except SystemExit:\n" //
                + "    pass\n" //
                + "  return json.dumps(output)\n" //
                + "def emptyFunction():\n" // to avoid compile error when empty user code block
                + "  pass";
    }

    private String setUpFlatMap() {
        return securityPyFunc() + "import json\n" //
                + "import collections\n" //
                + "def userFunction(inputJSON):\n" //
                + "  input = json.loads(inputJSON, object_pairs_hook=collections.OrderedDict)\n" //
                + "  outputList = []\n" //
                + "  try:\n" //
                + "    emptyFunction()\n" //
                + "    " + properties.pythonCode.getValue().replaceAll("\n", "\n    ").trim() + "\n"//
                + "  except SystemExit:\n" //
                + "    pass\n" //
                + "  return [ json.dumps(outputElement) for outputElement in outputList ]\n" //
                + "def emptyFunction():\n" // to avoid compile error when empty user code block
                + "  pass";
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
