package org.talend.components.service.spi;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
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
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link ServiceSpiFactory}.
 */
public class ServiceSpiFactoryTest {

    @BeforeClass
    public static void registryMvnHandler() {
        RuntimeUtil.registerMavenUrlHandler();
    }

    @Before
    public void resetRegistry() {
        ServiceSpiFactory.resetDefinitionRegistryOnlyForTest();
    }

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

    @Test
    public void testCreateClassLoaderService() throws MalformedURLException {
        // this will check that create a new registry with the new url
        // given
        DefinitionRegistry definitionRegistry = ServiceSpiFactory.getDefinitionRegistry();
        assertThat(definitionRegistry.getComponentFamilies(), not(hasKey(is("MultiRuntimeExample"))));

        // when
        DefinitionRegistry definitionRegistry2 = ServiceSpiFactory
                .createDefinitionRegistry(new URL[] { new URL("mvn:org.talend.components/multiple-runtime-comp/0.18.0") });

        // then
        assertThat(definitionRegistry2.getComponentFamilies(), hasKey(is("MultiRuntimeExample")));
        assertThat(definitionRegistry, not(equalTo(definitionRegistry2)));

        // this will check that same registry is returned if no classpath is passed
        assertThat(definitionRegistry2, equalTo(ServiceSpiFactory.createDefinitionRegistry(null)));
        assertThat(definitionRegistry2, equalTo(ServiceSpiFactory.createDefinitionRegistry(new URL[0])));

    }

    @Test
    public void testUpdateClassLoaderService() throws MalformedURLException {
        // this will check that create an update registry with the new url
        // given
        DefinitionRegistry definitionRegistry = ServiceSpiFactory
                .createDefinitionRegistry(new URL[] { new URL("mvn:org.talend.components/multiple-runtime-comp/0.18.0") });
        assertThat(definitionRegistry.getComponentFamilies(), hasKey(is("MultiRuntimeExample")));
        assertThat(definitionRegistry.getComponentFamilies(), not(hasKey(is("Jdbc"))));

        // when
        DefinitionRegistry definitionRegistry2 = ServiceSpiFactory.createUpdatedDefinitionRegistry(
                new URL[] { new URL("mvn:org.talend.components/components-jdbc-definition/0.18.0") });

        // then
        assertThat(definitionRegistry2.getComponentFamilies(), hasKey(is("MultiRuntimeExample")));
        assertThat(definitionRegistry2.getComponentFamilies(), hasKey(is("Jdbc")));
        // this will check that same registry is returned if no classpath is passed
        assertThat(definitionRegistry2, equalTo(ServiceSpiFactory.createDefinitionRegistry(null)));
        assertThat(definitionRegistry2, equalTo(ServiceSpiFactory.createDefinitionRegistry(new URL[0])));

    }

}