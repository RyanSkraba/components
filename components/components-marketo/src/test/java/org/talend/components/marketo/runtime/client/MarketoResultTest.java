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
package org.talend.components.marketo.runtime.client;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;

public class MarketoResultTest {

    @Test
    public void testToString() throws Exception {
        IndexedRecord ir = new Record(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());

        result = new MarketoRecordResult(true, "POS", 0, 0, Arrays.asList(ir));
        String s = "MarketoRecordResult{requestId='', success=true, errors=null, recordCount=0, remainCount=0, "
                + "streamPosition='POS', records=[{\"id\": null, \"email\": null, \"firstName\": null, \"lastName\": "
                + "null, \"createdAt\": null, \"updatedAt\": null}]}";
        assertEquals(s, result.toString());
    }

    MarketoRecordResult result;

    @Test
    public void testMarketoResultConstruct() {
        result = new MarketoRecordResult();
        assertEquals(emptyList(), result.getRecords());
        assertEquals(0, result.getRecordCount());
        assertEquals(0, result.getRemainCount());
        assertEquals("", result.getStreamPosition());
        assertEquals("", result.getRequestId());
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(0, result.getErrors().size());

        result.setRecordCount(100);
        result.setRemainCount(100);
        result.setStreamPosition("100");
        result.setRecords(new ArrayList<IndexedRecord>());
        result.setRequestId("REST::666");
        result.setSuccess(true);
        result.setErrors(Arrays.asList(new MarketoError("REST")));
        assertEquals(emptyList(), result.getRecords());
        assertEquals(100, result.getRecordCount());
        assertEquals(100, result.getRemainCount());
        assertEquals("100", result.getStreamPosition());
        assertEquals("REST::666", result.getRequestId());
        assertTrue(result.isSuccess());
        assertNotNull(result.getErrors());
        result = new MarketoRecordResult("200", 200, 200, null);
        assertNull(result.getRecords());
        assertEquals(200, result.getRecordCount());
        assertEquals(200, result.getRemainCount());
        assertEquals("200", result.getStreamPosition());
        result.setErrors(Arrays.asList(new MarketoError("REST", "101", "error"), new MarketoError("error2")));
        assertNotNull(result.getErrorsString());
    }
}
