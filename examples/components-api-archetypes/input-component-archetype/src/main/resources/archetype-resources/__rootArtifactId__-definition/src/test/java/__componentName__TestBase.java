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
package ${package};

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.test.AbstractComponentTest2;
import ${package}.${componentPackage}.${componentName}Definition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class ${componentName}TestBase extends AbstractComponentTest2 {

    @Inject
    private DefinitionRegistryService definitionRegistry;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return definitionRegistry;
    }
    
    @Test
    public void testComponentHasBeenRegistered(){
        assertComponentIsRegistered(ComponentDefinition.class, "${componentName}", ${componentName}Definition.class);
        assertComponentIsRegistered(Definition.class, "${componentName}", ${componentName}Definition.class);
    }
}
