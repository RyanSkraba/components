package org.talend.components.service.spi;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.localio.LocalIOComponentFamilyDefinition;
import org.talend.components.localio.fixedflowinput.FixedFlowInputDefinition;
import org.talend.components.simplefileio.SimpleFileIOComponentFamilyDefinition;
import org.talend.components.simplefileio.SimpleFileIODatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIODatastoreDefinition;
import org.talend.components.simplefileio.input.SimpleFileIOInputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIOOutputDefinition;
import org.talend.daikon.definition.Definition;

/**
 * Unit tests for {@link ServiceSpiFactory}.
 */
public class ServiceSpiFactoryTest {

    @Test
    public void testGetComponentService() throws Exception {
        ComponentService cs = ServiceSpiFactory.getComponentService();
        assertThat(cs, not(nullValue()));

        DefinitionRegistry defReg = ServiceSpiFactory.getDefinitionRegistry();
        assertThat(cs, not(nullValue()));

        Map<String, ComponentFamilyDefinition> families = defReg.getComponentFamilies();
        assertThat(families, hasEntry(is("LocalIO"), isA((Class) LocalIOComponentFamilyDefinition.class)));
        assertThat(families, hasEntry(is("SimpleFileIo"), isA((Class) SimpleFileIOComponentFamilyDefinition.class)));

        Map<String, Definition> definitions = defReg.getDefinitions();
        assertThat(definitions, hasEntry(is("FixedFlowInput"), isA((Class) FixedFlowInputDefinition.class)));
        assertThat(definitions, hasEntry(is("SimpleFileIoDatastore"), isA((Class) SimpleFileIODatastoreDefinition.class)));
        assertThat(definitions, hasEntry(is("SimpleFileIoDataset"), isA((Class) SimpleFileIODatasetDefinition.class)));
        assertThat(definitions, hasEntry(is("SimpleFileIoInput"), isA((Class) SimpleFileIOInputDefinition.class)));
        assertThat(definitions, hasEntry(is("SimpleFileIoOutput"), isA((Class) SimpleFileIOOutputDefinition.class)));
    }

}