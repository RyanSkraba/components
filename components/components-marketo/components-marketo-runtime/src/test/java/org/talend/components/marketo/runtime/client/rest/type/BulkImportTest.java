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
package org.talend.components.marketo.runtime.client.rest.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;

public class BulkImportTest {

    BulkImport bi;

    @Before
    public void setUp() throws Exception {
        bi = new BulkImport();
    }

    @Test
    public void testSettersAndGetters() throws Exception {
        assertNull(bi.getBatchId());
        assertNull(bi.getMessage());
        assertNull(bi.getOperation());
        assertNull(bi.getImportId());
        assertNull(bi.getImportTime());
        assertNull(bi.getObjectApiName());
        assertNull(bi.getNumOfLeadsProcessed());
        assertNull(bi.getNumOfObjectsProcessed());
        assertNull(bi.getNumOfRowsFailed());
        assertNull(bi.getNumOfRowsWithWarning());
        assertNull(bi.getStatus());
        assertEquals("", bi.getFailuresLogFile());
        assertEquals("", bi.getWarningsLogFile());
        //
        assertNull(bi.getNumOfRowsProcessed());
        bi.setNumOfLeadsProcessed(2);
        assertEquals(Integer.valueOf(2), bi.getNumOfRowsProcessed());
        bi.setNumOfLeadsProcessed(null);
        bi.setNumOfObjectsProcessed(4);
        assertEquals(Integer.valueOf(4), bi.getNumOfRowsProcessed());
        //
        bi.setBatchId(1000);
        bi.setMessage("message");
        bi.setOperation("operation");
        bi.setImportId("importid");
        bi.setImportTime("importime");
        bi.setObjectApiName("objectapiname");
        bi.setNumOfLeadsProcessed(100);
        bi.setNumOfObjectsProcessed(101);
        bi.setNumOfRowsFailed(102);
        bi.setNumOfRowsWithWarning(103);
        bi.setStatus("status");
        bi.setFailuresLogFile(bi.getFailuresOrWarningsFilename(false));
        bi.setWarningsLogFile(bi.getFailuresOrWarningsFilename(true));
        assertEquals("1000", bi.getBatchId().toString());
        assertEquals("message", bi.getMessage());
        assertEquals("operation", bi.getOperation());
        assertEquals("importid", bi.getImportId());
        assertEquals("importime", bi.getImportTime());
        assertEquals("objectapiname", bi.getObjectApiName());
        assertEquals("100", bi.getNumOfLeadsProcessed().toString());
        assertEquals("101", bi.getNumOfObjectsProcessed().toString());
        assertEquals("102", bi.getNumOfRowsFailed().toString());
        assertEquals("103", bi.getNumOfRowsWithWarning().toString());
        assertEquals("status", bi.getStatus());
        assertEquals("bulk_customobjects_objectapiname_1000_failures.csv", bi.getFailuresLogFile());
        assertEquals("bulk_customobjects_objectapiname_1000_warnings.csv", bi.getWarningsLogFile());
    }

    @Test
    public void testToString() throws Exception {
        String s1 = "BulkImport{batchId=null, importTime='null', importId='null', message='null', "
                + "numOfRowsProcessed=null, numOfLeadsProcessed=null, numOfObjectsProcessed=null, "
                + "numOfRowsFailed=null, numOfRowsWithWarning=null, objectApiName='null', operation='null', "
                + "status='null', failuresLogFile='', warningsLogFile=''}";
        String s2 = "BulkImport{batchId=1000, importTime='importime', importId='importid', message='message', "
                + "numOfRowsProcessed=100, numOfLeadsProcessed=100, numOfObjectsProcessed=101, numOfRowsFailed=102, "
                + "numOfRowsWithWarning=103, objectApiName='objectapiname', operation='operation', status='status', "
                + "failuresLogFile='', warningsLogFile=''}";
        assertEquals(s1, bi.toString());
        bi.setBatchId(1000);
        bi.setMessage("message");
        bi.setOperation("operation");
        bi.setImportId("importid");
        bi.setImportTime("importime");
        bi.setObjectApiName("objectapiname");
        bi.setNumOfLeadsProcessed(100);
        bi.setNumOfObjectsProcessed(101);
        bi.setNumOfRowsFailed(102);
        bi.setNumOfRowsWithWarning(103);
        bi.setStatus("status");
        assertEquals(s2, bi.toString());

    }

    @Test
    public void testIsBulkLeadsImport() throws Exception {
        assertTrue(bi.isBulkLeadsImport());
        bi.setObjectApiName("co");
        assertFalse(bi.isBulkLeadsImport());
    }

    @Test
    public void testGetFailuresOrWarningsFilename() throws Exception {
        assertEquals("bulk_leads_null_failures.csv", bi.getFailuresOrWarningsFilename(false));
        assertEquals("bulk_leads_null_warnings.csv", bi.getFailuresOrWarningsFilename(true));
        bi.setBatchId(100);
        assertEquals("bulk_leads_100_failures.csv", bi.getFailuresOrWarningsFilename(false));
        assertEquals("bulk_leads_100_warnings.csv", bi.getFailuresOrWarningsFilename(true));
        bi.setObjectApiName("car_c");
        assertEquals("bulk_customobjects_car_c_100_failures.csv", bi.getFailuresOrWarningsFilename(false));
        assertEquals("bulk_customobjects_car_c_100_warnings.csv", bi.getFailuresOrWarningsFilename(true));
    }

    @Test
    public void testToIndexedRecord() throws Exception {
        bi.setBatchId(1000);
        bi.setMessage("message");
        bi.setOperation("operation");
        bi.setImportId("importid");
        bi.setImportTime("importime");
        bi.setObjectApiName("objectapiname");
        bi.setNumOfLeadsProcessed(100);
        bi.setNumOfObjectsProcessed(101);
        bi.setNumOfRowsFailed(102);
        bi.setNumOfRowsWithWarning(103);
        bi.setStatus("status");

        IndexedRecord r = bi.toIndexedRecord();

    }

}
