package org.talend.components.hadoopcluster;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;

public class HadoopClusterFamilyDefinitionTest {

    private final HadoopClusterFamilyDefinition componentFamilyDefinition = new
            HadoopClusterFamilyDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    @Test
    public void testGetName() {
        String componentName = componentFamilyDefinition.getName();
        assertEquals(componentName, "HadoopCluster");
    }

    @Test
    public void testInstall(){
        componentFamilyDefinition.install(ctx);
        Mockito.verify(ctx, times(1)).registerComponentFamilyDefinition(any(ComponentFamilyDefinition.class));
    }

}
