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
package org.talend.components.jira.runtime.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.talend.components.jira.testutils.JiraTestConstants.WRONG_USER;

import java.io.IOException;

import org.junit.Test;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.Action;

/**
 * Integration tests for {@link JiraInsertWriter}
 */
public class JiraInsertWriterTestIT extends JiraWriterTestBase {

    @Override
    protected void setupProperties() {
        super.setupProperties();
        properties.action.setValue(Action.INSERT);
    }

    /**
     * Checks {@link JiraInsertWriter#write()} throws {@link DataRejectException} with message
     * "Record is invalid" in case server responses with 400 Bad Request status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteBadRequest() throws IOException {
        String expectedError = "Record is invalid";
        JiraWriter insertProjectWriter = writeOperation.createWriter(null);
        insertProjectWriter.open("ins");
        try {
            insertProjectWriter.write(badJsonRecord);
            fail();
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            assertEquals(expectedError, rejectError);
        } finally {
            insertProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraInsertWriter#write()} throws {@link DataRejectException} with message
     * "User is not authenticated. Record wasn't inserted" in case server responses with 401 Unauthorized status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteUnauthorized() throws IOException {
        changeUserTo(WRONG_USER);
        String expectedError = "User is not authenticated. Record wasn't inserted";
        JiraWriter insertProjectWriter = writeOperation.createWriter(null);
        insertProjectWriter.open("ins");
        try {
            insertProjectWriter.write(badJsonRecord);
            fail();
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            assertEquals(expectedError, rejectError);
        } finally {
            insertProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraInsertWriter#write()} throws {@link IOException} if {@link JiraInsertWriter#open()}
     * wasn't called
     * 
     * @throws IOException
     */
    @Test(expected = IOException.class)
    public void testWriteNotOpened() throws IOException {
        JiraWriter insertProjectWriter = writeOperation.createWriter(null);
        insertProjectWriter.write(wrongProjectRecord);
    }
}
