package org.talend.components.google.drive.runtime.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GoogleDriveGetResultTest {

    GoogleDriveGetResult result;

    @Before
    public void setUp() throws Exception {
        result = new GoogleDriveGetResult("uid", "content".getBytes());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("uid", result.getId());
    }

    @Test
    public void testGetContent() throws Exception {
        assertEquals("content", new String(result.getContent()));
    }

}
