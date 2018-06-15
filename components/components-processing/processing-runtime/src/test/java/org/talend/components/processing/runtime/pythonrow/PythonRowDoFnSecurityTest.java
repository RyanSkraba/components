package org.talend.components.processing.runtime.pythonrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.AccessControlException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.apache.beam.sdk.util.UserCodeException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.python.core.PyBaseExceptionDerived;
import org.python.core.PyException;
import org.python.core.PyType;
import org.talend.components.processing.definition.pythonrow.MapType;
import org.talend.components.processing.definition.pythonrow.PythonRowProperties;
import org.talend.daikon.avro.GenericDataRecordHelper;

public class PythonRowDoFnSecurityTest {

    // Just let the python component run, the content is not important.
    private static IndexedRecord inputIndexedRecord = null;

    @BeforeClass
    public static void setUp() throws IOException {
        Object[] inputAsObject1 = new Object[] { "DumyRecordContent" };
        inputIndexedRecord = GenericDataRecordHelper.createRecord(inputAsObject1);
    }

    public void execute(String command) throws Throwable {
        execute(command, MapType.MAP);
    }

    public void execute(String command, MapType type) throws Throwable {
        try {
            PythonRowProperties properties = new PythonRowProperties("test");
            properties.init();
            properties.mapType.setValue(type);
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
    @Ignore("Use absolutely path in policy file, no way to make unit test works")
    public void test_fileAccess() throws Throwable {
        expectedException.expect(AccessControlException.class);
        expectedException.expectMessage("access denied (\"java.io.FilePermission\" \"/etc/passwd\" \"read\")");
        String command = "f = open('/etc/passwd', 'r')\n"//
                + "output['content'] = f.readline()\n";
        execute(command);
    }

    @Test(expected = UserCodeException.class)
    public void test_limitImports() throws Throwable {
        String command = "import os\n" //
                + "output['content'] = 'no error?'\n";
        execute(command);
    }

    @Test(expected = UserCodeException.class)
    public void test_limitCode() throws Throwable {
        String command =
                "from java.security import AccessControlException, Permissions, AllPermission, SecureClassLoader, CodeSource\n"
                        + "from java.net import URL\n" + "import java.security\n" + "\n"
                        + "class MagicClassLoader(SecureClassLoader):\n" + "  def _init_(self):\n"
                        + "    SecureClassLoader._init_(self)\n" + "    self.datamap = {}\n"
                        + "    self.codeSource = CodeSource(URL('file:/pwn'), None)\n" + "\n"
                        + "  def addClass(self, name, data):\n" + "    self.datamap[name] = data\n" + "\n"
                        + "  def findClass(self, name):\n" + "    data = self.datamap[name]\n"
                        + "    return self.super_defineClass(name, data, 0, len(data), self.codeSource)\n" + "    \n"
                        + "  def getPermissions(self, codesource):\n" + "    permissions = Permissions()\n"
                        + "    permissions.add(AllPermission())\n" + "    return permissions    \n" + "\n"
                        + "output = input";
        try {
            execute(command);
        } catch (PyException pyEx) {
            assertEquals("ImportError", ((PyType) pyEx.type).getName());
            assertEquals("No module named os", ((PyBaseExceptionDerived) pyEx.value).getMessage().toString());
            return;
        }
        assertTrue(false);
    }

    @Test
    @Ignore("workaround")
    public void test_limitImplictImports() throws Throwable {
        String command = "import genericpath\n" //
                + "output['content'] = genericpath.os.getpid()\nprint output['content']";
        execute(command);
    }

    @Test
    public void test_sysExit() throws Throwable {
        String command =
                "import sys\n" //
                + "sys.exit(1)\n" //
                + "output['content']='no error?'";
        execute(command);
        assertTrue(true);
    }

    @Test
    public void test_threadExit() throws Throwable {
        String command = "import thread\n" //
                + "thread.exit()\n" //
                + "output['content'] = 'no error?'\n";
        execute(command);
        assertTrue(true);
    }

    @Test
    public void test_emptyCodeMap() throws Throwable {
        String command = "";
        execute(command);
    }

    @Test
    public void test_emptyCodeFlatMap() throws Throwable {
        String command = "";
        execute(command, MapType.FLATMAP);
    }

    @Test
    @Ignore("Show all loaded modules")
    public void test_limitAdvancedImports() throws Throwable {
        String command = "import importlib\n" + "\n" //
                + "for module_name in sys.builtin_module_names:\n" //
                + "    try:\n" //
                + "        print module_name\n" //
                + "        loaded_module = importlib.import_module(module_name)\n" //
                + "        functions = dir(loaded_module)\n" //
                + "        output[module_name] = functions\n" //
                + "        print module_name, output[module_name]\n" //
                + "    except ImportError, e:\n" //
                + "        output['error'] = e";
        execute(command);
    }

}
