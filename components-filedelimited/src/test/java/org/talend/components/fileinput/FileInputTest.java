package org.talend.components.fileinput;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.PrintWriter;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.components.fileinput.runtime.FileInputSource;
import org.talend.components.fileinput.tFileInputDelimited.TFileInputDelimitedDefinition;

@SuppressWarnings("nls")
public class FileInputTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test. 
    public ComponentService getComponentService() {
        if (componentService == null) {
            SimpleComponentRegistry testComponentRegistry = new SimpleComponentRegistry();
            testComponentRegistry.addComponent(TFileInputDelimitedDefinition.COMPONENT_NAME, new FileInputDefinition("tFileInputDelimited"));
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Ignore
    @Test
    public void testFileInputRuntime() throws Exception {
    	TFileInputDelimitedDefinition def = (TFileInputDelimitedDefinition) getComponentService().getComponentDefinition("FileInput");
        FileInputProperties props = (FileInputProperties) getComponentService().getComponentProperties("FileInput");

        // Set up the test schema - not really used for anything now
        Schema schema = SchemaBuilder.builder().record("testRecord").fields().name("field1").type().stringType().noDefault().endRecord();
        props.schema.schema.setValue(schema);

        File temp = File.createTempFile("FileInputtestFile", ".txt");
        try {
            PrintWriter writer = new PrintWriter(temp.getAbsolutePath(), "UTF-8");
            writer.println("The first line");
            writer.println("The second line");
            writer.close();

            props.filename.setValue(temp.getAbsolutePath());
            Source source = def.getRuntime();
            source.initialize(null, props);
            assertThat(source, instanceOf(FileInputSource.class));

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

}
