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
package org.talend.components.azurestorage.blob.definitions.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.blob.helpers.RemoteBlob;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobGet;

public class RemoteBlobTest {

    private RemoteBlob remoteBlob;

    private RemoteBlobGet remoteBlobGet;

    private RemoteBlobGet remoteBlobGet2;

    @Before
    public void setUp() throws Exception {
        remoteBlob = new RemoteBlob("", true);
        remoteBlobGet = new RemoteBlobGet("", true, true);
        remoteBlobGet2 = new RemoteBlobGet("", true);
    }

    @Test
    public void testCreator() {
        assertEquals("", remoteBlob.prefix);
        assertEquals(true, remoteBlob.include);
        assertEquals("", remoteBlobGet.prefix);
        assertEquals(true, remoteBlobGet.include);
        assertEquals(true, remoteBlobGet.create);
        assertEquals("", remoteBlobGet2.prefix);
        assertEquals(true, remoteBlobGet2.include);
        assertEquals(false, remoteBlobGet2.create);
    }

}
