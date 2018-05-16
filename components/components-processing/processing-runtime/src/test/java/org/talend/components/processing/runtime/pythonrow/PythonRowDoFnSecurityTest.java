package org.talend.components.processing.runtime.pythonrow;

import java.io.IOException;
import java.security.AccessControlException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.python.core.PyException;
import org.talend.components.processing.definition.pythonrow.MapType;
import org.talend.components.processing.definition.pythonrow.PythonRowProperties;
import org.talend.daikon.avro.GenericDataRecordHelper;

@Ignore("Use absolutely path in policy file, no way to make unit test works")
public class PythonRowDoFnSecurityTest {

    // Just let the python component run, the content is not important.
    private static IndexedRecord inputIndexedRecord = null;

    @BeforeClass
    public static void setUp() throws IOException {
        Object[] inputAsObject1 = new Object[] { "DumyRecordContent" };
        inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject1);
    }

    public void execute(String command) throws Throwable {
        try {
            PythonRowProperties properties = new PythonRowProperties("test");
            properties.init();
            properties.mapType.setValue(MapType.MAP);
            properties.pythonCode.setValue(command);
            PythonRowDoFn function = new PythonRowDoFn();
            function.initialize(null, properties);
            DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(function);
            fnTester.processBundle((IndexedRecord) inputIndexedRecord);
        } catch (PyException pyEx) {
            throw pyEx.getCause() == null ? pyEx : pyEx.getCause();
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test_fileAccess() throws Throwable {
        expectedException.expect(AccessControlException.class);
        expectedException.expectMessage("access denied (\"java.io.FilePermission\" \"/etc/passwd\" \"read\")");
        String command = "f = open('/etc/passwd', 'r')\n"//
                + "output['content'] = f.readline()\n";
        execute(command);
    }

    @Test
    public void test_limitImports() throws Throwable {
        String command = "import os\n" //
                + "output['content'] = 'no error?'\n";
        execute(command);
    }

    @Test
    public void test_limitImplictImports() throws Throwable {
        String command = "import genericpath\n" //
                + "import signal\n" //
                + "genericpath.os.kill(123123,signal.SIGTERM)\n";
        execute(command);
    }

}
