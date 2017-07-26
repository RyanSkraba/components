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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * Unit-tests for {@link JdbcRuntimeInfo}
 */
public class JdbcRuntimeInfoTest {

    @Test
    public void testJdbcRuntimeInfoRuntimeSettingProviderString() {
        RuntimeSettingProvider properties = createPropsWithDriverName("org.talend.Driver1");
        new JdbcRuntimeInfo(properties, null);
    }

    @Test
    public void testRemoveQuote() {
        RuntimeSettingProvider properties = createPropsWithDriverName("org.talend.Driver1");
        Assert.assertEquals("url", new JdbcRuntimeInfo(properties, null).removeQuote("\"url\""));
    }

    /**
     * Checks {@link JdbcRuntimeInfo#isClassLoaderReusable()} always returns <code>true</code>
     * JDBC component supports several databases. Each database requires different set of classes.
     * So, when component in tcomp-service is called for different databases, {@link ClassLoader}
     * should be cached, but {@link JdbcRuntimeInfo} should return different hash code results for
     * different driver classes
     */
    @Test
    public void testIsClassLoaderReusable() {
        RuntimeSettingProvider anyProperties = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(anyProperties, "anyRuntimeClassName");
        assertTrue(jdbcRuntimeInfo.isClassLoaderReusable());
    }

    /**
     * Checks 2 {@link JdbcRuntimeInfo}s have different value of {@link JdbcRuntimeInfo#hashCode()} if they have different driver
     * class names
     */
    @Test
    public void testHashCodeDifferent() {
        RuntimeSettingProvider runtimeSettingProvider1 = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo1 = new JdbcRuntimeInfo(runtimeSettingProvider1, "anyRuntimeClassName");
        int hashCode1 = jdbcRuntimeInfo1.hashCode();

        RuntimeSettingProvider runtimeSettingProvider2 = createPropsWithDriverName("org.talend.Driver2");
        JdbcRuntimeInfo jdbcRuntimeInfo2 = new JdbcRuntimeInfo(runtimeSettingProvider2, "anyRuntimeClassName");
        int hashCode2 = jdbcRuntimeInfo2.hashCode();

        assertNotEquals(hashCode1, hashCode2);
    }

    /**
     * Checks 2 {@link JdbcRuntimeInfo}s have same value of {@link JdbcRuntimeInfo#hashCode()} if they have same driver class
     * names
     */
    @Test
    public void testHashCodeSame() {
        RuntimeSettingProvider runtimeSettingProvider1 = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo1 = new JdbcRuntimeInfo(runtimeSettingProvider1, "anyRuntimeClassName");
        int hashCode1 = jdbcRuntimeInfo1.hashCode();

        RuntimeSettingProvider runtimeSettingProvider2 = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo2 = new JdbcRuntimeInfo(runtimeSettingProvider2, "anyRuntimeClassName");
        int hashCode2 = jdbcRuntimeInfo2.hashCode();

        assertEquals(jdbcRuntimeInfo1, jdbcRuntimeInfo2);
        assertEquals(hashCode1, hashCode2);
    }

    /**
     * Checks symmetric rule for {@link JdbcRuntimeInfo#equals(Object)}, when 2 objects have same driver class name
     */
    @Test
    public void testEqualsIsSymmetric() {
        RuntimeSettingProvider runtimeSettingProvider1 = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo1 = new JdbcRuntimeInfo(runtimeSettingProvider1, "anyRuntimeClassName");

        RuntimeSettingProvider runtimeSettingProvider2 = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo2 = new JdbcRuntimeInfo(runtimeSettingProvider2, "anyRuntimeClassName");

        assertTrue(jdbcRuntimeInfo1.equals(jdbcRuntimeInfo2));
        assertTrue(jdbcRuntimeInfo2.equals(jdbcRuntimeInfo1));
    }

    /**
     * Checks 2 {@link JdbcRuntimeInfo}s with different driver class names are not equal
     */
    @Test
    public void testEqualsFalse() {
        RuntimeSettingProvider runtimeSettingProvider1 = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo1 = new JdbcRuntimeInfo(runtimeSettingProvider1, "anyRuntimeClassName");

        RuntimeSettingProvider runtimeSettingProvider2 = createPropsWithDriverName("org.talend.Driver2");
        JdbcRuntimeInfo jdbcRuntimeInfo2 = new JdbcRuntimeInfo(runtimeSettingProvider2, "anyRuntimeClassName");

        assertFalse(jdbcRuntimeInfo1.equals(jdbcRuntimeInfo2));
        assertFalse(jdbcRuntimeInfo2.equals(jdbcRuntimeInfo1));
    }

    /**
     * Checks {@link JdbcRuntimeInfo#equals(Object)} returns false, when instance of super class is passed with
     * similar field values as this {@link JdbcRuntimeInfo} object
     */
    @Test
    public void testEqualsSuper() {
        RuntimeSettingProvider runtimeSettingProvider = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(runtimeSettingProvider, "anyRuntimeClassName");

        JarRuntimeInfo jarRuntimeInfo = new JarRuntimeInfo(jdbcRuntimeInfo.getJarUrl().toString(),
                jdbcRuntimeInfo.getDepTxtPath(), jdbcRuntimeInfo.getRuntimeClassName());

        assertFalse(jdbcRuntimeInfo.equals(jarRuntimeInfo));
    }

    /**
     * Checks reflexive rule for {@link JdbcRuntimeInfo#equals(Object)}
     */
    @Test
    public void testEqualsIsReflexive() {
        RuntimeSettingProvider runtimeSettingProvider = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(runtimeSettingProvider, "anyRuntimeClassName");

        assertTrue(jdbcRuntimeInfo.equals(jdbcRuntimeInfo));
    }

    /**
     * Checks transitive rule for {@link JdbcRuntimeInfo#equals(Object)}
     */
    @Test
    public void testEqualsIsTransitive() {
        RuntimeSettingProvider runtimeSettingProviderX = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfoX = new JdbcRuntimeInfo(runtimeSettingProviderX, "anyRuntimeClassName");

        RuntimeSettingProvider runtimeSettingProviderY = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfoY = new JdbcRuntimeInfo(runtimeSettingProviderY, "anyRuntimeClassName");

        RuntimeSettingProvider runtimeSettingProviderZ = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfoZ = new JdbcRuntimeInfo(runtimeSettingProviderZ, "anyRuntimeClassName");

        assertTrue(jdbcRuntimeInfoX.equals(jdbcRuntimeInfoY));
        assertTrue(jdbcRuntimeInfoY.equals(jdbcRuntimeInfoZ));
        assertTrue(jdbcRuntimeInfoX.equals(jdbcRuntimeInfoZ));
    }

    /**
     * Checks {@link JdbcRuntimeInfo#equals(Object)} return <code>false</code> when <code>null</code> is passed
     */
    @Test
    public void testEqualsNull() {
        RuntimeSettingProvider runtimeSettingProvider = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(runtimeSettingProvider, "anyRuntimeClassName");

        assertFalse(jdbcRuntimeInfo.equals(null));
    }

    @Test
    public void testToString() {
        RuntimeSettingProvider properties = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(properties, "anyRuntimeClassName");
        assertTrue(jdbcRuntimeInfo.toString().contains("JdbcRuntimeInfo: {driverClassName:org.talend.Driver1}"));
    }

    /**
     * Checks {@link JdbcRuntimeInfo#cloneWithNewJarUrlString(String)} creates new instance of {@link JdbcRuntimeInfo}
     * with the same values of <code>depTxtPath</code>, <code>runtimeClassName</code>, but different values of <code>jarUrl</code>
     */
    @Test
    public void testCloneWithNewJarUrlString() {
        RuntimeSettingProvider properties = createPropsWithDriverName("org.talend.Driver1");
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(properties, "anyRuntimeClassName");
        JdbcRuntimeInfo clone = jdbcRuntimeInfo.cloneWithNewJarUrlString("mvn: new/url");
        assertTrue(jdbcRuntimeInfo != clone);
        assertNotEquals(jdbcRuntimeInfo, clone);
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
