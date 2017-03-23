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

package org.talend.components.netsuite;

import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class NetSuiteRuntimeInfoTest {

    @Test
    public void testRuntimeVersion() throws Exception {
        testRuntimeVersion("2014_2");
        testRuntimeVersion("2016_2");
    }

    public void testRuntimeVersion(String apiVersion) throws Exception {
        NetSuiteConnectionProperties connProps = new NetSuiteConnectionProperties("test");
        connProps.setupProperties();
        connProps.endpoint.setValue("https://webservices.netsuite.com/services/NetSuitePort_" + apiVersion);

        RuntimeInfo runtimeInfo = NetSuiteComponentDefinition.getRuntimeInfo(connProps,
                NetSuiteComponentDefinition.RUNTIME_CLASS);
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));
        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertNotNull(jarRuntimeInfo.getJarUrl());
        assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(jarRuntimeInfo.getRuntimeClassName(),
                "org.talend.components.netsuite.v" + apiVersion + ".NetSuiteRuntimeImpl");
    }
}
