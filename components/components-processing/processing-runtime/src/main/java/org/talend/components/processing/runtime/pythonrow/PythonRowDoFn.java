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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.pythonrow.MapType;
import org.talend.components.processing.pythonrow.PythonRowProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.properties.ValidationResult;

public class PythonRowDoFn extends DoFn<Object, Object> implements RuntimableRuntime<PythonRowProperties> {

    private PythonRowProperties properties = null;

    private PythonInterpreter python = null;

    private IndexedRecordConverter converter = null;

    private Schema schema = null;

    private GenericDatumReader<IndexedRecord> reader = null;


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

            if (schema == null) {
                schema = retrieveOutputSchema(input);
            }
            if (reader == null) {
                reader = new GenericDatumReader<>(schema);
            }

            if (MapType.MAP.equals(properties.mapType.getValue())) {
                map(input, context);
            } else { // flatmap
                flatMap(input, context);
            }
        }
        python.cleanup();
    }

    private Schema retrieveOutputSchema(IndexedRecord input) {
        if (properties.changeOutputSchema.getValue()) {
            return properties.schemaFlow.schema.getValue();
        } else {
            return input.getSchema();
        }
    }

    private void map(IndexedRecord input, ProcessContext context) throws IOException {
        // Prepare Python environment
        python.set("inputJSON", new PyString(input.toString()));
        python.exec("input = json.loads(inputJSON)");
        python.exec("output = json.loads(\"{}\")");

        // Execute user command
        python.exec(properties.pythonCode.getValue());

        // Retrieve results
        python.exec("outputJSON = json.dumps(output)");
        PyObject output = python.get("outputJSON");

        ByteArrayInputStream bais = new ByteArrayInputStream(output.toString().getBytes());
        DataInputStream din = new DataInputStream(bais);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, din);
        context.output(reader.read(null, decoder));
    }

    private void flatMap(IndexedRecord input, ProcessContext context) throws IOException {
        // Prepare Python environment
        python.set("inputJSON", new PyString(input.toString()));
        python.exec("input = json.loads(inputJSON)");
        python.exec("outputList = []");

        // Execute user command
        python.exec(properties.pythonCode.getValue());

        // Retrieve results
        python.exec("outputJSON = [ json.dumps(outputElement) for outputElement in outputList ]");
        PyObject outputList = python.get("outputJSON");

        if (outputList instanceof PyList) {
            PyList list = (PyList) outputList;
            for (Object output : list) {
                ByteArrayInputStream bais = new ByteArrayInputStream(output.toString().getBytes());
                DataInputStream din = new DataInputStream(bais);
                JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, din);
                context.output(reader.read(null, decoder));
            }
        }
    }

    @Teardown
    public void tearDown() {
        python.close();
    }
}
