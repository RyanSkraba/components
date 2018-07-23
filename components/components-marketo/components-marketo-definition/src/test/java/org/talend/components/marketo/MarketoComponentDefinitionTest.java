// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputDefinition;

public class MarketoComponentDefinitionTest {

    MarketoComponentDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new TMarketoInputDefinition();
    }

    @Test
    public final void testMarketoComponentDefinition() {
        assertTrue(def.getSupportedExecutionEngines().contains(ExecutionEngine.DI));
        assertFalse(def.getSupportedExecutionEngines().contains(ExecutionEngine.DI_SPARK_BATCH));
        assertFalse(def.getSupportedExecutionEngines().contains(ExecutionEngine.DI_SPARK_STREAMING));
        assertFalse(def.getSupportedExecutionEngines().contains(ExecutionEngine.BEAM));
    }

    @Test
    public final void testGetReturnProperties() {
        assertEquals(def.getReturnProperties()[0].getName(), MarketoComponentDefinition.RETURN_ERROR_MESSAGE);
        assertEquals(def.getReturnProperties()[1].getName(), MarketoComponentDefinition.RETURN_NB_CALL);
    }

    @Test
    public void testGetFamilies() {
        assertThat(def.getFamilies(), arrayContainingInAnyOrder("Business/Marketo", "Cloud/Marketo"));
    }

    @Test
    public void testIsSchemaAutoPropagate() throws Exception {
        assertFalse(def.isSchemaAutoPropagate());
    }

    @Test
    public void testIsStartable() throws Exception {
        assertTrue(def.isStartable());
    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() throws Exception {
        assertEquals(TMarketoConnectionProperties.class, def.getNestedCompatibleComponentPropertiesClass()[0]);
    }
}
