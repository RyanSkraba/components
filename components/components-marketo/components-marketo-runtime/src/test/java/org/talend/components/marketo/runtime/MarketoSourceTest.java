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
package org.talend.components.marketo.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.text.DateFormat;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.runtime.FastDateParser;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkImportTo;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoSourceTest extends MarketoRuntimeTestBase {

    MarketoSource source;

    TMarketoInputProperties iprops;

    TMarketoBulkExecProperties bulkProps;

    @Before
    public void setUp() throws Exception {
        source = new MarketoSource();
        //
        iprops = new TMarketoInputProperties("test");
        iprops.connection.setupProperties();
        iprops.connection.endpoint.setValue("http://ABC.mkto.com");
        iprops.connection.clientAccessId.setValue("fakeId");
        iprops.connection.secretKey.setValue("sekret");
        iprops.setupProperties();
        //
        bulkProps = new TMarketoBulkExecProperties("test");
        bulkProps.setupProperties();
        bulkProps.connection.init();
    }

    @Test
    public void splitIntoBundles() throws Exception {
        assertTrue(source.splitIntoBundles(1000, null).size() > 0);
    }

    @Test
    public void getEstimatedSizeBytes() throws Exception {
        assertEquals(0, source.getEstimatedSizeBytes(null));
    }

    @Test
    public void producesSortedKeys() throws Exception {
        assertFalse(source.producesSortedKeys(null));
    }

    @Test
    public void testTDI38561() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.connection.endpoint.setValue("htp:ttoot.com");
        props.connection.clientAccessId.setValue("user");
        props.connection.secretKey.setValue("secret");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
        props.connection.endpoint.setValue("https://ttoot.com");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
        props.connection.endpoint.setValue("https://ttoot.com/rustinpeace/rest");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
        props.connection.endpoint.setValue("https://ttoot.com/rest");
        source.initialize(null, props);
        ValidationResult vr = source.validate(null);
        assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
    }

    @Test
    public void testIsInvalidDate() throws Exception {
        assertTrue(source.isInvalidDate("20170516 112417"));
        assertTrue(source.isInvalidDate("20170516 11:24:17"));
        assertTrue(source.isInvalidDate("20170516 11:24:17 0000"));
        assertTrue(source.isInvalidDate("2017-05-16 11:24:17 0000"));
        assertTrue(source.isInvalidDate("2017-05-16 11:24:17"));
        assertTrue(source.isInvalidDate("2017-05-16'T'11:24:17 +0100"));
        DateFormat format = FastDateParser.getInstance("yyyy-MM-dd HH:mm:ss Z");
        format.setTimeZone(TimeZone.getTimeZone("Europe/England"));
        assertFalse(source.isInvalidDate(FastDateParser.getInstance("yyyy-MM-dd HH:mm:ss Z").format(new java.util.Date())));
        format.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        assertFalse(source.isInvalidDate(FastDateParser.getInstance("yyyy-MM-dd HH:mm:ss Z").format(new java.util.Date())));
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17 +0100"));
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17 -0100"));
        //
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17+0000"));
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17-0000"));
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17+0100"));
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17-0100"));
        assertFalse(source.isInvalidDate("2017-07-10 13:53:26Z"));
        assertFalse(source.isInvalidDate("2017-05-16T11:24:17+0100"));
    }

    @Test
    public void testCreateReader() throws Exception {
        source.initialize(null, null);
        assertNull(source.createReader(null));
        source.initialize(null, iprops);
        assertTrue(source.createReader(null) instanceof MarketoInputReader);
        source.initialize(null, bulkProps);
        assertTrue(source.createReader(null) instanceof MarketoBulkExecReader);
    }

    @Test
    public void testValidate() throws Exception {
        source.initialize(null, iprops);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
        MarketoSource spy = spy(source);
        //
        doReturn(iprops.connection).when(spy).getEffectiveConnection(any(RuntimeContainer.class));
        doReturn(ValidationResult.OK).when(spy).validateConnection(any(MarketoProvideConnectionProperties.class));
        //
        //
        iprops.leadKeyValue.setValue("");
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        iprops.leadKeyValue.setValue("OkKey");
        spy.initialize(null, iprops);
        assertEquals(Result.OK, spy.validate(null).getStatus());
        //
        iprops.connection.apiMode.setValue(APIMode.SOAP);
        iprops.schemaInput.schema.setValue(getLeadDynamicSchema());
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        //
        iprops.inputOperation.setValue(InputOperation.getMultipleLeads);
        iprops.leadSelectorSOAP.setValue(LeadSelector.LeadKeySelector);
        iprops.leadKeyValues.setValue("");
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.connection.apiMode.setValue(APIMode.REST);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.leadSelectorREST.setValue(LeadSelector.StaticListSelector);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.listParam.setValue(ListParam.STATIC_LIST_NAME);
        iprops.listParamListName.setValue("");
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.leadSelectorREST.setValue(LeadSelector.LastUpdateAtSelector);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        //
        iprops.inputOperation.setValue(InputOperation.getLeadActivity);
        iprops.schemaInput.schema.setValue(getLeadDynamicSchema());
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.connection.apiMode.setValue(APIMode.SOAP);
        iprops.schemaInput.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        iprops.leadKeyValue.setValue("");
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.connection.apiMode.setValue(APIMode.REST);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        //
        iprops.inputOperation.setValue(InputOperation.getLeadChanges);
        iprops.schemaInput.schema.setValue(getLeadDynamicSchema());
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.connection.apiMode.setValue(APIMode.SOAP);
        iprops.schemaInput.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.connection.apiMode.setValue(APIMode.REST);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.sinceDateTime.setValue("2017-05-16 11:24:17 +0100");
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        //
        iprops.inputOperation.setValue(InputOperation.CustomObject);
        iprops.schemaInput.schema.setValue(getLeadDynamicSchema());
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.schemaInput.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        iprops.customObjectName.setValue("");
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.customObjectAction.setValue(CustomObjectAction.list);
        iprops.schemaInput.schema.setValue(getLeadDynamicSchema());
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.customObjectAction.setValue(CustomObjectAction.get);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.customObjectName.setValue("co");
        iprops.useCompoundKey.setValue(true);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.useCompoundKey.setValue(false);
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        iprops.customObjectFilterType.setValue("filter");
        spy.initialize(null, iprops);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        // BulkImport
        //
        spy.initialize(null, bulkProps);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        bulkProps.bulkFilePath.setValue("fpath");
        spy.initialize(null, bulkProps);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        bulkProps.bulkFilePath.setValue(System.getProperty("java.io.tmpdir"));
        spy.initialize(null, bulkProps);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        bulkProps.logDownloadPath.setValue("dpath");
        spy.initialize(null, bulkProps);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
        //
        bulkProps.logDownloadPath.setValue(System.getProperty("java.io.tmpdir"));
        spy.initialize(null, bulkProps);
        assertEquals(Result.OK, spy.validate(null).getStatus());
        //
        bulkProps.bulkImportTo.setValue(BulkImportTo.CustomObjects);
        spy.initialize(null, bulkProps);
        assertEquals(Result.ERROR, spy.validate(null).getStatus());
    }
}
