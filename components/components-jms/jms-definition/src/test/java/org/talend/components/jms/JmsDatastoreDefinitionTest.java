// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.jms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.daikon.runtime.RuntimeInfo;

public class JmsDatastoreDefinitionTest {

    private final JmsDatastoreDefinition datastoreDefinition = new JmsDatastoreDefinition();

    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = datastoreDefinition.getRuntimeInfo(null, null);
        assertEquals("org.talend.components.jms.runtime_1_1.DatastoreRuntime", runtimeInfo.getRuntimeClassName());
    }

    @Test
    public void testCreateProperties() {
        JmsDatastoreProperties props = datastoreDefinition.createProperties();
        assertEquals("JmsDatastore", props.getName());
    }
}
