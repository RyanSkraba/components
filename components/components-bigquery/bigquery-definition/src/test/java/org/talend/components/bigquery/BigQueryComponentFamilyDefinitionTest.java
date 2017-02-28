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

package org.talend.components.bigquery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class BigQueryComponentFamilyDefinitionTest{

    private final BigQueryComponentFamilyDefinition componentFamilyDefinition = new BigQueryComponentFamilyDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    /**
     * Check {@link BigQueryComponentFamilyDefinitionTest} returns "BigQuery"
     */
    @Test
    public void testGetName() {
        String componentName = componentFamilyDefinition.getName();
        assertEquals(componentName, "BigQuery");
    }

    /**
     * Check {@link BigQueryComponentFamilyDefinition#install(ComponentInstaller.ComponentFrameworkContext ctx)} which call once the method "registerComponentFamilyDefinition"
     */
    @Test
    public void testInstall(){
        componentFamilyDefinition.install(ctx);
        Mockito.verify(ctx, times(1)).registerComponentFamilyDefinition(any(ComponentFamilyDefinition.class));
    }
}
