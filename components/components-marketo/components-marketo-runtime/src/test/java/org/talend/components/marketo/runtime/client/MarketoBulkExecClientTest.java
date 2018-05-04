// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.talend.components.marketo.runtime.client.MarketoBaseRESTClient.REST;
import static org.talend.components.marketo.runtime.client.MarketoBulkExecClient.BULK_STATUS_COMPLETE;
import static org.talend.components.marketo.runtime.client.MarketoBulkExecClient.BULK_STATUS_FAILED;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.runtime.client.MarketoBulkExecClient;
import org.talend.components.marketo.runtime.client.rest.response.BulkImportResult;
import org.talend.components.marketo.runtime.client.rest.type.BulkImport;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkFileFormat;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkImportTo;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.RESTLookupFields;

public class MarketoBulkExecClientTest {

    public static final String FILE_PATH_INEXISTANT = "/inexisting-file";

    TMarketoBulkExecProperties properties;

    MarketoRecordResult recordResult;

    MarketoBulkExecClient bulkClient;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoBulkExecClientTest.class);

    private String leadsFile;

    private String coFile;

    @Before
    public void setUp() throws Exception {

        properties = new TMarketoBulkExecProperties("test");
        properties.schemaInput.setupProperties();
        properties.schemaInput.setupLayout();
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.connection.setupProperties();
        properties.connection.endpoint.setValue("https://fake.io/rest");
        properties.connection.clientAccessId.setValue("clientaccess");
        properties.connection.secretKey.setValue("sekret");
        properties.connection.attemptsIntervalTime.setValue(10); // shorten interval for tests
        properties.setupProperties();
        properties.setupLayout();
        properties.pollWaitTime.setValue(1);
        properties.logDownloadPath.setValue("/tmp");
        properties.customObjectName.setValue("smartphone_c");

        leadsFile = getClass().getClassLoader().getResource("./leads.csv").getPath();
        coFile = getClass().getClassLoader().getResource("./customobjects.csv").getPath();
        //
        bulkClient = spy(new MarketoBulkExecClient(properties.getConnectionProperties()));
    }

    @Test
    public void testExecutePostFileRequest() throws Exception {
    }

    @Test
    public void testExecuteDownloadFileRequest() throws Exception {
    }

    @Test
    public void testGetStatusesForBatch() throws Exception {
        doNothing().when(bulkClient).executeDownloadFileRequest(any(File.class));
        BulkImport r = bulkClient.getStatusesForBatch(getBulkImportFail(), "");
        LOG.warn("[testBulkImport] {}", r);
        r = bulkClient.getStatusesForBatch(getBulkImportFailForCO(), "");
    }

    @Test
    public void testBulkImport() throws Exception {
        doReturn(getBulkImportResultOK()).when(bulkClient).executePostFileRequest(BulkImportResult.class, leadsFile);
        doReturn(getBulkImportResultOK()).when(bulkClient).executeGetRequest(BulkImportResult.class);
        doReturn(getBulkImportResultFail()).when(bulkClient).executePostFileRequest(BulkImportResult.class, FILE_PATH_INEXISTANT);
        doReturn(getBulkImportResultFail()).when(bulkClient).executePostFileRequest(BulkImportResult.class, coFile);
        doReturn(getBulkImportResultRunning()).when(bulkClient).executePostFileRequest(BulkImportResult.class, null);
        //
        properties.bulkFilePath.setValue(FILE_PATH_INEXISTANT);
        properties.bulkFileFormat.setValue(BulkFileFormat.csv);
        properties.bulkImportTo.setValue(BulkImportTo.Leads);
        properties.lookupField.setValue(RESTLookupFields.email);
        // failing
        recordResult = bulkClient.bulkImport(properties);
        LOG.warn("[testBulkImport] {}", recordResult);
        assertFalse(recordResult.isSuccess());
        assertEquals(0, recordResult.getRecordCount());
        assertEquals(0, recordResult.getRemainCount());
        assertNotNull(recordResult.getRecords());
        // ok
        properties.bulkFilePath.setValue(leadsFile);
        properties.listId.setValue(666);
        properties.partitionName.setValue("partition1");
        recordResult = bulkClient.bulkImport(properties);
        LOG.warn("[testBulkImport] {}", recordResult);
        assertTrue(recordResult.isSuccess());
        assertEquals(1, recordResult.getRecordCount());
        assertEquals(0, recordResult.getRemainCount());
        assertNotNull(recordResult.getRecords());

        // ok - but process error
        properties.bulkFilePath.setValue(leadsFile);
        properties.listId.setValue(666);
        properties.partitionName.setValue("partition1");
        doReturn(getBulkImportResultOKButImportFail()).when(bulkClient).executePostFileRequest(BulkImportResult.class, leadsFile);
        recordResult = bulkClient.bulkImport(properties);
        LOG.warn("[testBulkImport] {}", recordResult);
        assertFalse(recordResult.isSuccess());
        assertEquals(1, recordResult.getRecordCount());
        assertEquals(0, recordResult.getRemainCount());
        assertNotNull(recordResult.getRecords());

        // running
        properties.bulkFilePath.setValue(null);
        recordResult = bulkClient.bulkImport(properties);
        LOG.warn("[testBulkImport] {}", recordResult);
        assertTrue(recordResult.isSuccess());
        assertEquals(1, recordResult.getRecordCount());
        assertEquals(0, recordResult.getRemainCount());
        assertNotNull(recordResult.getRecords());
        //
        properties.bulkImportTo.setValue(BulkImportTo.CustomObjects);
        properties.bulkFilePath.setValue(coFile);
        recordResult = bulkClient.bulkImport(properties);
        LOG.warn("[testBulkImport] {}", recordResult);
        assertFalse(recordResult.isSuccess());
        assertEquals(0, recordResult.getRecordCount());
        assertEquals(0, recordResult.getRemainCount());
        assertNotNull(recordResult.getRecords());
        //
        doReturn(getBulkImportResultRunningForCO()).when(bulkClient).executePostFileRequest(BulkImportResult.class, null);
        properties.bulkFilePath.setValue(null);
        recordResult = bulkClient.bulkImport(properties);
        LOG.warn("[testBulkImport] {}", recordResult);
        assertTrue(recordResult.isSuccess());
        assertEquals(1, recordResult.getRecordCount());
        assertEquals(0, recordResult.getRemainCount());
        assertNotNull(recordResult.getRecords());
        //
        doThrow(new MarketoException("REST", "Exception")).when(bulkClient).executePostFileRequest(BulkImportResult.class, null);
        properties.bulkFilePath.setValue(null);
        recordResult = bulkClient.bulkImport(properties);
        LOG.warn("[testBulkImport] {}", recordResult);
        assertFalse(recordResult.isSuccess());
        assertEquals(0, recordResult.getRecordCount());
        assertEquals(0, recordResult.getRemainCount());
        assertNotNull(recordResult.getRecords());
    }

    private BulkImportResult getBulkImportResultFail() {
        BulkImportResult bulkResult = new BulkImportResult();
        bulkResult.setSuccess(false);
        bulkResult.setResult(Arrays.asList(getBulkImportFail()));
        bulkResult.setNextPageToken(null);
        bulkResult.setMoreResult(false);
        bulkResult.setErrors(Arrays.asList(new MarketoError(REST, "Path doesn't exist.")));
        return bulkResult;
    }

    private BulkImportResult getBulkImportResultOK() {
        BulkImportResult bulkResult = new BulkImportResult();
        bulkResult.setSuccess(true);
        bulkResult.setResult(Arrays.asList(getBulkImportOK()));
        bulkResult.setNextPageToken(null);
        bulkResult.setMoreResult(false);
        bulkResult.setErrors(Collections.emptyList());
        return bulkResult;
    }

    private BulkImportResult getBulkImportResultOKButImportFail() {
        BulkImportResult bulkResult = new BulkImportResult();
        bulkResult.setSuccess(true);
        bulkResult.setResult(Arrays.asList(getBulkImportFail()));
        bulkResult.setNextPageToken(null);
        bulkResult.setMoreResult(false);
        bulkResult.setErrors(Collections.emptyList());
        return bulkResult;
    }

    private BulkImportResult getBulkImportResultRunning() {
        BulkImportResult bulkResult = new BulkImportResult();
        bulkResult.setSuccess(true);
        bulkResult.setResult(Arrays.asList(getBulkImportRunning()));
        bulkResult.setNextPageToken(null);
        bulkResult.setMoreResult(false);
        bulkResult.setErrors(Collections.emptyList());
        return bulkResult;
    }

    private BulkImportResult getBulkImportResultRunningForCO() {
        BulkImportResult bulkResult = getBulkImportResultRunning();
        bulkResult.setResult(Arrays.asList(getBulkImportRunningForCO()));
        return bulkResult;
    }

    private BulkImport getBulkImportFail() {
        BulkImport b = new BulkImport();
        b.setBatchId(12345);
        b.setFailuresLogFile("");
        b.setMessage("upload !ok");
        b.setStatus(BULK_STATUS_FAILED);
        b.setNumOfRowsFailed(3);
        b.setNumOfRowsWithWarning(1);
        b.setOperation("");

        return b;
    }

    private BulkImport getBulkImportFailForCO() {
        BulkImport b = getBulkImportFail();
        b.setObjectApiName("smartphone_c");

        return b;
    }

    private BulkImport getBulkImportOK() {
        BulkImport b = new BulkImport();
        b.setBatchId(12345);
        b.setFailuresLogFile("");
        b.setMessage("upload ok");
        b.setStatus(BULK_STATUS_COMPLETE);
        b.setNumOfLeadsProcessed(3);
        b.setNumOfObjectsProcessed(3);
        b.setNumOfRowsFailed(0);
        b.setNumOfRowsWithWarning(0);
        return b;
    }

    private BulkImport getBulkImportRunning() {
        BulkImport b = new BulkImport();
        b.setBatchId(12345);
        b.setFailuresLogFile("");
        b.setMessage("executing request");
        b.setStatus("running");
        b.setNumOfRowsFailed(0);
        b.setNumOfRowsWithWarning(0);
        return b;
    }

    private BulkImport getBulkImportRunningForCO() {
        BulkImport b = getBulkImportRunning();
        b.setObjectApiName("smartphone_c");
        return b;
    }

}
