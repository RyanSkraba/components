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

package org.talend.components.elasticsearch;

import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class ElasticsearchComponentFamilyDefinitionTest {

    private final ElasticsearchComponentFamilyDefinition componentFamilyDefinition = new ElasticsearchComponentFamilyDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    /**
     * Check {@link ElasticsearchComponentFamilyDefinitionTest} returns "Elasticsearch"
     */
    @Test
    public void testGetName() {
        String componentName = componentFamilyDefinition.getName();
        assertEquals(componentName, "Elasticsearch");
    }

    /**
     * Check {@link ElasticsearchComponentFamilyDefinition#install(ComponentInstaller.ComponentFrameworkContext ctx)} which call once the method "registerComponentFamilyDefinition"
     */
    @Test
    public void testInstall(){
        componentFamilyDefinition.install(ctx);
        Mockito.verify(ctx, times(1)).registerComponentFamilyDefinition(any(ComponentFamilyDefinition.class));
    }
}
