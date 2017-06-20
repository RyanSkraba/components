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

package org.talend.components.salesforce.dataset;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.talend.components.salesforce.SalesforceDefinition.DATASET_RUNTIME_CLASS;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class SalesforceDatasetDefinitionTest extends SalesforceTestBase {

    private SalesforceDatasetDefinition definition;
    private SalesforceDatasetProperties properties;

    @Before
    public void setUp() {
        definition = new SalesforceDatasetDefinition();

        properties = new SalesforceDatasetProperties("root");
        properties.init();
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(properties);
        assertRuntimeInfo(runtimeInfo);
    }

    private void assertRuntimeInfo(RuntimeInfo runtimeInfo) {
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertNotNull(jarRuntimeInfo.getJarUrl());
        assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(DATASET_RUNTIME_CLASS, jarRuntimeInfo.getRuntimeClassName());
    }

    @Test
    public void testImagePath() {
        assertNotNull(definition.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
    }

}
