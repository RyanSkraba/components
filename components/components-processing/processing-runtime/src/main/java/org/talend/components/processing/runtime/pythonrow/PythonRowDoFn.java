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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

import net.sourceforge.prograde.sm.ProGradeJSM;

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

        // export deny.security.policy file and public key talend-jython.ks
        String policyFilePath = ExportResource("deny.security.abs.policy");
        // ExportResource("/talend-jython.ks");
        // set the policy file, "=" is needed for override the default policy
        System.setProperty("java.security.policy", "=" + policyFilePath);
        System.setSecurityManager(new ProGradeJSM()); // use pro grade security manager
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
        return "import sys\n" //
                + "deimported_modules = ['os', 'signal']\n" //
                + "deimported_java_packages = ['java.security','java.security.SecureClassLoader','java.security.Permission']\n" //
                + "for deimported_module in deimported_modules:\n" //
                + " sys.modules[deimported_module] = None\n" //
                + "for deimported_java_package in deimported_java_packages:\n" //
                + " sys.modules[deimported_java_package] = None\n" //
                + "delattr(sys, 'exit')\n" //
                + "\n";
    }

    private String setUpMap() {
        return securityPyFunc() + "import json\n" //
                + "import collections\n" //
                + "def userFunction(inputJSON):\n" //
                + "    input = json.loads(inputJSON, object_pairs_hook=collections.OrderedDict)\n" //
                + "    output = json.loads(\"{}\", object_pairs_hook=collections.OrderedDict)\n" //
                + "    " + properties.pythonCode.getValue().replaceAll("\n", "\n    ").trim() //
                + "\n    return json.dumps(output)";
    }

    private String setUpFlatMap() {
        return securityPyFunc() + "import json\n" //
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

    // Copy the policy file and public key from the jython jar to the current runtime folder
    public String ExportResource(String resourceName) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        try {
            stream = getClass().getResourceAsStream(resourceName);// note that each / is a directory down in the "jar
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }
            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
                    .getParentFile()
                    .getPath()
                    .replace('\\', '/');
            resStreamOut = new FileOutputStream(jarFolder + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            stream.close();
            resStreamOut.close();
        }
        return jarFolder + resourceName;
    }
}
