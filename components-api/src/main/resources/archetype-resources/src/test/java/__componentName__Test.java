#set( $symbol_pound = '#' )
        #set( $symbol_dollar = '$' )
        #set( $symbol_escape = '\' )

package ${package};

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaElement.Type;
import org.talend.daikon.schema.SchemaFactory;

public class ${componentName}Test {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    @Before
    public void initializeComponentRegistryAnsService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test. Shall be overriden of Spring or OSGI tests
    public ComponentService getComponentService() {
        if (componentService == null) {
            SimpleComponentRegistry testComponentRegistry = new SimpleComponentRegistry();
            testComponentRegistry.addComponent(${componentName}Definition.COMPONENT_NAME, new ${componentName}Definition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void test${componentName}Runtime() throws Exception {
        ${componentName}Definition def = (${componentName}Definition) getComponentService().getComponentDefinition("${componentName}");
        ${componentName}Properties props = (${componentName}Properties) getComponentService().getComponentProperties("${componentName}");
        File temp = File.createTempFile("${componentName}testFile", ".txt");
        try {
            PrintWriter writer = new PrintWriter(temp.getAbsolutePath(), "UTF-8");
            writer.println("The first line");
            writer.println("The second line");
            writer.close();

            props.filename.setValue(temp.getAbsolutePath());
            ComponentRuntime runtime = def.createRuntime();
            List<Map<String, Object>> rows = new ArrayList<>();
            runtime.input(props, rows);
            System.out.println(rows);
            assertEquals(2, rows.size());
            assertEquals("The first line", rows.get(0).get("line"));
            assertEquals("The second line", rows.get(1).get("line"));
        } finally {// remote the temp file
            temp.delete();
        }
    }

    @Test
    public void test${componentName}RuntimeException() {
        ${componentName}Definition def = (${componentName}Definition) getComponentService().getComponentDefinition("${componentName}");
        ${componentName}Properties props = (${componentName}Properties) getComponentService().getComponentProperties("${componentName}");
        // check empty schema exception
        props.schema.schema.setChildren(Collections.EMPTY_LIST);
        ComponentRuntime runtime = def.createRuntime();
        try {
            runtime.inputBegin(props);
            fail("Should have thrown an exception");
        } catch (Exception e) {
            if (!(e instanceof TalendRuntimeException
                    && ((TalendRuntimeException) e).getCode() == ComponentsErrorCode.SCHEMA_MISSING)) {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                fail("wrong exception caught :" + stack.toString());
            }
        }
        // check wrong schema exception
        props.schema.schema.setChildren(new ArrayList<SchemaElement>());
        props.schema.schema.addChild(SchemaFactory.newSchemaElement(Type.INT, "line"));
        try {
            runtime.inputBegin(props);
            fail("Should have thrown an exception");
        } catch (Exception e) {
            if (!(e instanceof TalendRuntimeException
                    && ((TalendRuntimeException) e).getCode() == ComponentsErrorCode.SCHEMA_TYPE_MISMATCH)) {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                fail("wrong exception caught :" + stack.toString());
            }
        }

    }
    
    
    @Test
    public void testAlli18n() {
        ComponentTestUtils.checkAllI18N(new ${componentName}Properties(null).init(), errorCollector);
    }
    
    @Test
    public void testAllImagePath() {
        ComponentTestUtils.testAllImages(getComponentService());
    }

    @Test
    public void testAllRuntimes() {
        ComponentTestUtils.testAllRuntimeAvaialble(getComponentService());
    }
    
}