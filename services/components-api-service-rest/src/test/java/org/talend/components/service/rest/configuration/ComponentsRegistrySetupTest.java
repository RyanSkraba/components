package org.talend.components.service.rest.configuration;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;


public class ComponentsRegistrySetupTest {


    @Test
    public void testExtractComponentsUrls() {
        // checking with a null string
        ComponentsRegistrySetup registrySetup = new ComponentsRegistrySetup();
        assertThat(registrySetup.extractComponentsUrls(null), arrayWithSize(0));

        // checking with working URLs
        assertThat(registrySetup.extractComponentsUrls("file://foo,file://bar"), arrayWithSize(2));

        // checking with one working URL and one wrong one
        assertThat(registrySetup.extractComponentsUrls("file://foo,groovybaby://bar"), arrayWithSize(1));

    }

    // TODO need more tests on the createDefinitionRegistry

}
