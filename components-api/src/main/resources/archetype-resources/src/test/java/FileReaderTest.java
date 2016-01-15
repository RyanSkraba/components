#set( $symbol_pound = '#' )
        #set( $symbol_dollar = '$' )
        #set( $symbol_escape = '\' )

        package ${package};

        import java.io.File;
        import java.io.PrintWriter;
        import java.util.List;
        import java.util.Map;
        import java.util.ArrayList;

        import static org.junit.Assert.*;
        import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
        import org.talend.components.api.runtime.ComponentRuntime;
        import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.test.ComponentTestUtils;
import org.talend.components.test.SimpleComponentRegistry;

public class ${classNamePrefix}Test {

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
            testComponentRegistry.addComponent(${classNamePrefix}Definition.COMPONENT_NAME, new ${classNamePrefix}Definition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void testComponent() throws Exception {
        ${classNamePrefix}Definition def = (${classNamePrefix}Definition)getComponentService().getComponentDefinition("${classNamePrefix}");
        ${classNamePrefix}Properties props = (${classNamePrefix}Properties)getComponentService().getComponentProperties("${classNamePrefix}");
        File temp = File.createTempFile("${classNamePrefix}testFile", ".txt");

        PrintWriter writer = new PrintWriter(temp.getAbsolutePath(), "UTF-8");
        writer.println("The first line");
        writer.println("The second line");
        writer.close();

        props.filename.setValue(temp.getAbsolutePath());
        ComponentRuntime runtime = def.createRuntime();
        List<Map<String,Object>> rows = new ArrayList<>();
        runtime.input(props, rows);
        System.out.println(rows);
        assertEquals(2, rows.size());
        assertEquals("The first line", rows.get(0).get("line"));
        assertEquals("The second line", rows.get(1).get("line"));
    }

}