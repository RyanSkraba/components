// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package ${package}.definition;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import ${packageTalend}.api.ComponentFamilyDefinition;
import ${packageTalend}.api.ComponentInstaller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class ${componentNameClass}ComponentFamilyDefinitionTest{

    private final ${componentNameClass}ComponentFamilyDefinition componentFamilyDefinition = new ${componentNameClass}ComponentFamilyDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    /**
     * Check {@link ${componentNameClass}ComponentFamilyDefinitionTest} returns "${componentName}"
     */
    @Test
    public void testGetName() {
        String componentName = componentFamilyDefinition.getName();
        assertEquals(componentName, "${componentName}");
    }

    /**
     * Check {@link ${componentNameClass}ComponentFamilyDefinition#install()} which call once the method "registerComponentFamilyDefinition"
     */
    @Test
    public void testInstall(){
        componentFamilyDefinition.install(ctx);
        Mockito.verify(ctx, times(1)).registerComponentFamilyDefinition(any(ComponentFamilyDefinition.class));
    }

}
