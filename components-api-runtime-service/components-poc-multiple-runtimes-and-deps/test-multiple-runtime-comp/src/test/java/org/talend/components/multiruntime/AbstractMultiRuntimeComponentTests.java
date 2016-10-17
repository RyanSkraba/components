package org.talend.components.multiruntime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.multiruntime.MultiRuntimeComponentProperties.Version;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

abstract public class AbstractMultiRuntimeComponentTests extends AbstractComponentTest {

    @Test
    public void testMultiRuntimeComponentRuntime() throws Exception {
        MultiRuntimeComponentDefinition def = (MultiRuntimeComponentDefinition) getComponentService()
                .getComponentDefinition(MultiRuntimeComponentDefinition.COMPONENT_NAME);
        MultiRuntimeComponentProperties props = (MultiRuntimeComponentProperties) getComponentService()
                .getComponentProperties(MultiRuntimeComponentDefinition.COMPONENT_NAME);

        props.version.setValue(Version.VERSION_0_1);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil
                .createRuntimeClass(def.getRuntimeInfo(props, ConnectorTopology.OUTGOING), props.getClass().getClassLoader())) {
            Source source = (Source) sandboxedInstance.getInstance();
            source.initialize(null, props);
            assertEquals("Me", source.validate(null).getMessage());
        }

        props.version.setValue(Version.VERSION_0_2);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil
                .createRuntimeClass(def.getRuntimeInfo(props, ConnectorTopology.OUTGOING), props.getClass().getClassLoader())) {
            Source source2 = (Source) sandboxedInstance.getInstance();
            source2.initialize(null, props);
            assertEquals("AnotherMe", source2.validate(null).getMessage());
        }
    }

    @Test
    public void testMultiRuntimeComponentRuntimeFromProperties() throws Exception {
        MultiRuntimeComponentProperties props = (MultiRuntimeComponentProperties) getComponentService()
                .getComponentProperties("MultiRuntimeComponent");

        assertEquals("Me", props.getVersion1RuntimeResult());
        assertEquals("AnotherMe", props.getVersion2RuntimeResult());
    }
}
