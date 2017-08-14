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
package org.talend.components.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * Unit-tests for {@link JdbcRuntimeInfo}
 */
public class JdbcRuntimeInfoTest {

    @Test
    public void testRemoveQuote() {
        RuntimeSettingProvider properties = createPropsWithDriverName("org.talend.Driver1");
        Assert.assertEquals("url", new JdbcRuntimeInfo(properties, null).removeQuote("\"url\""));
    }

    /**
     * Checks {@link JdbcRuntimeInfo#isClassLoaderReusable()} always returns <code>true</code> JDBC component supports
     * several databases. Each database requires different set of classes. So, when component in tcomp-service is called
     * for different databases, {@link ClassLoader} should be cached, but {@link JdbcRuntimeInfo} should return
     * different hash code results for different driver classes
     */
    @Test
    public void testIsClassLoaderReusable() {
        RuntimeSettingProvider anyProperties = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(anyProperties, "anyRuntimeClassName");
        assertTrue(jdbcRuntimeInfo.isClassLoaderReusable());
    }

    @Test
    public void testToString() {
        RuntimeSettingProvider properties = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(properties, "anyRuntimeClassName");
        assertTrue(jdbcRuntimeInfo.toString().contains("JdbcRuntimeInfo: {driverClassName:org.talend.Driver1}"));
    }

    /**
     * Checks {@link JdbcRuntimeInfo#cloneWithNewJarUrlString(String)} creates new instance of {@link JdbcRuntimeInfo}
     * with the same values of <code>depTxtPath</code>, <code>runtimeClassName</code>, but different values of
     * <code>jarUrl</code>
     */
    @Test
    public void testCloneWithNewJarUrlString() {
        RuntimeSettingProvider properties = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(properties, "anyRuntimeClassName");
        JdbcRuntimeInfo clone = jdbcRuntimeInfo.cloneWithNewJarUrlString("mvn: new/url");
        assertTrue(jdbcRuntimeInfo != clone);
        assertEquals(jdbcRuntimeInfo.getDepTxtPath(), clone.getDepTxtPath());
        assertEquals(jdbcRuntimeInfo.getRuntimeClassName(), clone.getRuntimeClassName());
        assertNotEquals(jdbcRuntimeInfo.getJarUrl(), clone.getJarUrl());
    }

    /**
     * Creates instance of {@link RuntimeSettingProvider} with specified driver class name
     * 
     * @param driverName driver class name
     * @return new instance of {@link RuntimeSettingProvider}
     */
    private static RuntimeSettingProvider createPropsWithDriverName(String driverName) {
        AllSetting allSetting = Mockito.mock(AllSetting.class);
        Mockito.when(allSetting.getDriverClass()).thenReturn(driverName);
        RuntimeSettingProvider runtimeSettingProvider = Mockito.mock(RuntimeSettingProvider.class);
        Mockito.when(runtimeSettingProvider.getRuntimeSetting()).thenReturn(allSetting);
        return runtimeSettingProvider;
    }

}
