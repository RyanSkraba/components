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
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.avro.converter.JsonGenericRecordConverter;
import org.talend.daikon.avro.inferrer.JsonSchemaInferrer;
import org.talend.daikon.properties.ValidationResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PythonRowDoFn extends DoFn<Object, Object> implements RuntimableRuntime<PythonRowProperties> {

    private PythonRowProperties properties = null;

    private PythonInterpreter python = null;

    private IndexedRecordConverter converter = null;

    private JsonGenericRecordConverter jsonGenericRecordConverter = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PythonRowProperties componentProperties) {
        this.properties = (PythonRowProperties) componentProperties;
        return ValidationResult.OK;
    }
    
    @Setup
    public void setup() throws Exception {
        python = new PythonInterpreter();
        python.exec("import json");
    }

    @ProcessElement
    public void processElement(ProcessContext context) throws IOException {
        if (context.element() != null) {
            if (converter == null) {
                AvroRegistry registry = new AvroRegistry();
                converter = registry.createIndexedRecordConverter(context.element().getClass());
            }
            IndexedRecord input = (IndexedRecord) converter.convertToAvro(context.element());

            if (MapType.MAP.equals(properties.mapType.getValue())) {
                map(input, context);
            } else { // flatmap
                flatMap(input, context);
            }
        }
        python.cleanup();
    }

    private void map(IndexedRecord input, ProcessContext context) throws IOException {
        // Prepare Python environment
        python.set("inputJSON", new PyString(input.toString()));
        python.exec("import collections");
        python.exec("input = json.loads(inputJSON, object_pairs_hook=collections.OrderedDict)");
        python.exec("output = json.loads(\"{}\", object_pairs_hook=collections.OrderedDict)");

        // Execute user command
        python.exec(properties.pythonCode.getValue());

        // Retrieve results
        python.exec("outputJSON = json.dumps(output)");
        PyObject output = python.get("outputJSON");

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
        python.set("inputJSON", new PyString(input.toString()));
        python.exec("import collections");
        python.exec("input = json.loads(inputJSON, object_pairs_hook=collections.OrderedDict)");
        python.exec("outputList = []");

        // Execute user command
        python.exec(properties.pythonCode.getValue());

        // Retrieve results
        python.exec("outputJSON = [ json.dumps(outputElement) for outputElement in outputList ]");
        PyObject outputList = python.get("outputJSON");

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
        python.close();
    }
}
