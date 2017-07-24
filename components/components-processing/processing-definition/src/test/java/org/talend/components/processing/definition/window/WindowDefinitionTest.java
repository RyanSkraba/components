package org.talend.components.processing.definition.window;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.processing.definition.ProcessingFamilyDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.Set;

public class WindowDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final WindowDefinition definition = new WindowDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    /**
     * Check {@link WindowDefinition#getName()} returns "Window"
     */
    @Test
    public void testGetName() {
        String componentName = definition.getName();
        assertEquals(componentName, "Window");
    }

    /**
     * Check {@link WindowDefinition#getIconKey()} returns "window"
     */
    @Test
    public void testGetIconKey() {
        String componentName = definition.getIconKey();
        assertEquals(componentName, "window");
    }

    /**
     * Check {@link WindowDefinition#getFamilies()} returns string array,
     * which contains "Processing"
     */
    @Test
    public void testGetFamilies() {
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining(ProcessingFamilyDefinition.NAME));
    }

    /**
     * Check {@link WindowDefinition#getPropertyClass()} returns class,
     * which canonical name is
     * "org.talend.components.processing.window.WindowProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.processing.definition.window.WindowProperties"));
    }

    @Test
    public void testGetPngImagePath() {
        assertEquals("Window_icon32.png", definition.getPngImagePath(ComponentImageType.PALLETE_ICON_32X32));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> connector = definition.getSupportedConnectorTopologies();
        assertEquals(1, connector.size());
        assertTrue(connector.contains(ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, null, null);
        assertEquals("org.talend.components.processing.runtime.window.WindowRuntime", runtimeInfo.getRuntimeClassName());
    }
}
