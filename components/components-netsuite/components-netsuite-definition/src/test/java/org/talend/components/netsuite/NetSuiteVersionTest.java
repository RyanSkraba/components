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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 *
 */
public class NetSuiteVersionTest {

    @Test
    public void testGetMajorAsString() {
        NetSuiteVersion version = new NetSuiteVersion(2016, 2);
        assertEquals("2016.2", version.getMajorAsString("."));
    }

    @Test
    public void testGetAsString() {
        NetSuiteVersion version = new NetSuiteVersion(2016, 2, -1);
        assertEquals("2016.2", version.getAsString("."));
    }

    @Test
    public void testParseVersionMajor() {
        NetSuiteVersion version = NetSuiteVersion.parseVersion("2016.2");
        assertEquals(2016, version.getMajorYear());
        assertEquals(2, version.getMajorRelease());
        assertEquals(-1, version.getMinor());
    }

    @Test
    public void testParseVersionMajorAndPatch() {
        NetSuiteVersion version = NetSuiteVersion.parseVersion("2016.2");
        assertEquals(2016, version.getMajorYear());
        assertEquals(2, version.getMajorRelease());
        assertEquals(-1, version.getMinor());
    }

    @Test
    public void testDetectVersionMajor() {
        NetSuiteVersion version = NetSuiteVersion.detectVersion(
                "https://webservices.netsuite.com/services/NetSuitePort_2016_2");
        assertEquals(2016, version.getMajorYear());
        assertEquals(2, version.getMajorRelease());
        assertEquals(-1, version.getMinor());
    }

    @Test
    public void testDetectVersionMajorAndPatch() {
        NetSuiteVersion version = NetSuiteVersion.detectVersion(
                "https://webservices.netsuite.com/services/NetSuitePort_2016_2_1");
        assertEquals(2016, version.getMajorYear());
        assertEquals(2, version.getMajorRelease());
        assertEquals(1, version.getMinor());
    }

    @Test
    public void testI18n() {
        try {
            NetSuiteVersion.detectVersion("https://webservices.netsuite.com/services/NetSuitePort_2016.2");
        } catch (IllegalArgumentException e) {
            assertNotEquals("error.failedToDetectApiVersion", e.getMessage());
        }

        try {
            NetSuiteVersion.parseVersion("2016-2");
        } catch (IllegalArgumentException e) {
            assertNotEquals("error.failedToParseApiVersion", e.getMessage());
        }
    }
}
