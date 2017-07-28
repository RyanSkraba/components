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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkImportTo;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoRESTClientBulkExecTestIT extends MarketoBaseTestIT {

    TMarketoBulkExecProperties props;

    static String downloadPath;

    String coCSV;

    String leadCSV;

    @Before
    public void setUp() throws Exception {
        props = new TMarketoBulkExecProperties("test");
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_REST);
        props.connection.clientAccessId.setValue(USERID_REST);
        props.connection.secretKey.setValue(SECRETKEY_REST);
        props.connection.setupLayout();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.setupProperties();
        props.setupLayout();
        //
        coCSV = getClass().getResource("/customobjects.csv").getPath();
        leadCSV = getClass().getResource("/leads.csv").getPath();
        downloadPath = getClass().getResource("/").getPath() + "logs";
        Path dl = Paths.get(downloadPath);
        if (!Files.exists(dl)) {
            Files.createDirectory(dl);
        }
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File(downloadPath));
    }

    @Test
    public void testBulkExecCustomObject() throws Exception {
        props.bulkImportTo.setValue(BulkImportTo.CustomObjects);
        props.customObjectName.setValue("car_c");
        props.bulkFilePath.setValue(coCSV);
        props.logDownloadPath.setValue(downloadPath);
        props.pollWaitTime.setValue(5);
        props.afterBulkImportTo();
        Schema s = MarketoConstants.getBulkImportCustomObjectSchema();
        MarketoSource source = new MarketoSource();
        source.initialize(null, props);
        assertEquals(Result.OK, source.validate(null).getStatus());
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        MarketoRecordResult result = client.bulkImport(props);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecordCount());
        IndexedRecord r = result.getRecords().get(0);
        assertNotNull(r);
        assertEquals(1, r.get(s.getField("numOfObjectsProcessed").pos()));
        assertEquals(2, r.get(s.getField("numOfRowsFailed").pos()));
        assertEquals(0, r.get(s.getField("numOfRowsWithWarning").pos()));
        assertEquals("car_c", r.get(s.getField("objectApiName").pos()));
        assertEquals("import", r.get(s.getField("operation").pos()));
        assertEquals("Complete", r.get(s.getField("status").pos()));
        Object batchId = r.get(s.getField("batchId").pos());
        assertNotNull(batchId);
        Path logf = Paths.get(downloadPath,
                String.format("bulk_customobjects_car_c_%d_failures.csv", Integer.valueOf(batchId.toString())));
        assertEquals(logf.toString(), r.get(s.getField("failuresLogFile").pos()));
        assertTrue(Files.exists(logf));
        assertEquals(216, Files.readAllBytes(logf).length);
    }

    @Test
    public void testBulkExecLead() throws Exception {
        props.bulkImportTo.setValue(BulkImportTo.Leads);
        props.bulkFilePath.setValue(leadCSV);
        props.logDownloadPath.setValue(downloadPath);
        props.pollWaitTime.setValue(1);
        props.afterBulkImportTo();
        Schema s = MarketoConstants.getBulkImportLeadSchema();
        MarketoSource source = new MarketoSource();
        source.initialize(null, props);
        assertEquals(Result.OK, source.validate(null).getStatus());
        MarketoRESTClient client = (MarketoRESTClient) source.getClientService(null);
        MarketoRecordResult result = client.bulkImport(props);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecordCount());
        IndexedRecord r = result.getRecords().get(0);
        assertNotNull(r);
        assertEquals(2, r.get(s.getField("numOfLeadsProcessed").pos()));
        assertEquals(0, r.get(s.getField("numOfRowsFailed").pos()));
        assertEquals(1, r.get(s.getField("numOfRowsWithWarning").pos()));
        assertEquals("Complete", r.get(s.getField("status").pos()));
        Object batchId = r.get(s.getField("batchId").pos());
        assertNotNull(batchId);
        Path logf = Paths.get(downloadPath, String.format("bulk_leads_%d_warnings.csv", Integer.valueOf(batchId.toString())));
        assertEquals(logf.toString(), r.get(s.getField("warningsLogFile").pos()));
        assertTrue(Files.exists(logf));
        assertEquals(86, Files.readAllBytes(logf).length);
    }

    @Test
    public void testBulkExecValidate() throws Exception {
        props.bulkImportTo.setValue(BulkImportTo.Leads);
        props.bulkFilePath.setValue(leadCSV);
        props.logDownloadPath.setValue("/Users/undx/mp/");
        props.pollWaitTime.setValue(1);
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.connection.endpoint.setValue(ENDPOINT_SOAP);
        props.connection.clientAccessId.setValue(USERID_SOAP);
        props.connection.secretKey.setValue(SECRETKEY_SOAP);
        props.connection.afterApiMode();
        props.afterBulkImportTo();
        assertEquals(Result.ERROR, props.validateBulkImportTo().getStatus());
        MarketoSource source = new MarketoSource();
        source.initialize(null, props);
        assertEquals(Result.ERROR, source.validate(null).getStatus());
        props.connection.apiMode.setValue(APIMode.REST);
        props.connection.endpoint.setValue(ENDPOINT_REST);
        props.connection.clientAccessId.setValue(USERID_REST);
        props.connection.secretKey.setValue(SECRETKEY_REST);
        props.bulkFilePath.setValue("");
        source.initialize(null, props);
        assertEquals(Result.ERROR, source.validate(null).getStatus());
        props.bulkFilePath.setValue("inexistant.csv");
        source.initialize(null, props);
        assertEquals(Result.ERROR, source.validate(null).getStatus());
        props.bulkFilePath.setValue(leadCSV);
        source.initialize(null, props);
        assertEquals(Result.ERROR, source.validate(null).getStatus());
        props.logDownloadPath.setValue(downloadPath);
        source.initialize(null, props);
        assertEquals(Result.OK, source.validate(null).getStatus());
    }

}
