#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.daikon.exception.TalendRuntimeException;

@SuppressWarnings("nls")
public class ${componentName}Test {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    @Before
    public void initializeComponentRegistryAndService() {
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
            Source source = def.getRuntime();
            source.initialize(null, props);
            assertThat(source, instanceOf(${componentName}Source.class));

            Reader<?> reader = ((BoundedSource) source).createReader(null);
            assertThat(reader.start(), is(true));
            assertThat(reader.getCurrent(), is((Object) "The first line"));
            // No auto advance when calling getCurrent more than once.
            assertThat(reader.getCurrent(), is((Object) "The first line"));
            assertThat(reader.advance(), is(true));
            assertThat(reader.getCurrent(), is((Object) "The second line"));
            assertThat(reader.advance(), is(false));
        } finally {// remote the temp file
            temp.delete();
        }
    }

    @Ignore("To revisit.  The spec should be validated by the time it gets to the runtime.")
    @Test
    public void test${componentName}RuntimeException() {
        ${componentName}Definition def = (${componentName}Definition) getComponentService().getComponentDefinition("${componentName}");
        ${componentName}Properties props = (${componentName}Properties) getComponentService().getComponentProperties("${componentName}");

        // check empty schema exception
        props.schema.schema.setChildren(Collections.EMPTY_LIST);
        BoundedSource source = (BoundedSource) def.getRuntime();
        source.initialize(null, props);
        Reader<?> reader;
        reader = source.createReader(null);
        try {
            reader.start();
            fail("Should have thrown an exception");
        } catch (Exception e) {
            if (!(e instanceof TalendRuntimeException && ((TalendRuntimeException) e).getCode() == ComponentsErrorCode.SCHEMA_MISSING)) {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                fail("wrong exception caught :" + stack.toString());
            }
        }

        // check wrong schema exception
        // props.schema.schema.setChildren(new ArrayList<SchemaElement>());
        // props.schema.schema.addChild(SchemaFactory.newSchemaElement(Type.INT, "line"));

        source = (BoundedSource) def.getRuntime();
        source.initialize(null, props);
        reader = source.createReader(null);

        try {
            reader.start();
            fail("Should have thrown an exception");
        } catch (Exception e) {
            if (!(e instanceof TalendRuntimeException && ((TalendRuntimeException) e).getCode() == ComponentsErrorCode.SCHEMA_TYPE_MISMATCH)) {
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
