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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_PARAM;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.SOAP;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.CustomObject;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getLead;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getLeadActivity;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getLeadChanges;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getMultipleLeads;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP.EMAIL;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP.IDNUM;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LeadKeySelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.StaticListSelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam.STATIC_LIST_NAME;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.CustomObjectAction;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeREST;
import org.talend.daikon.avro.SchemaConstants;

public class MarketoInputReaderTestIT extends MarketoBaseTestIT {

    private transient static final Logger LOG = getLogger(MarketoInputReaderTestIT.class);

    MarketoInputReader reader;

    MarketoSource source;

    public TMarketoInputProperties getSOAPProperties() {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_SOAP);
        props.connection.clientAccessId.setValue(USERID_SOAP);
        props.connection.secretKey.setValue(SECRETKEY_SOAP);
        props.connection.setupLayout();
        props.mappingInput.setupProperties();
        props.setupProperties();
        props.connection.apiMode.setValue(SOAP);
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.includeTypes.setupProperties();
        props.includeTypes.type.setValue(new ArrayList<String>());
        props.excludeTypes.setupProperties();
        props.excludeTypes.type.setValue(new ArrayList<String>());
        props.setupLayout();

        return props;
    }

    public TMarketoInputProperties getRESTProperties() {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_REST);
        props.connection.clientAccessId.setValue(USERID_REST);
        props.connection.secretKey.setValue(SECRETKEY_REST);
        props.connection.apiMode.setValue(REST);
        props.connection.setupLayout();
        props.mappingInput.setupProperties();
        props.setupProperties();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.includeTypes.setupProperties();
        props.includeTypes.type.setValue(new ArrayList<String>());
        props.excludeTypes.setupProperties();
        props.excludeTypes.type.setValue(new ArrayList<String>());
        props.setupLayout();

        return props;
    }

    public MarketoInputReader getReader(TMarketoInputProperties properties) {
        MarketoSource source = new MarketoSource();
        source.initialize(null, properties);
        return (MarketoInputReader) source.createReader(null);
    }

    @Test
    public void testLeadSOAPSingle() throws Exception {
        TMarketoInputProperties props = getSOAPProperties();
        props.inputOperation.setValue(getLead);
        props.leadKeyTypeSOAP.setValue(EMAIL);
        props.batchSize.setValue(10);
        props.afterInputOperation();
        String email = EMAIL_LEAD_MANY_INFOS;
        props.leadKeyValue.setValue(email);
        reader = getReader(props);

        assertTrue(reader.start());
        LOG.debug("record = {}.", reader.getCurrent());
        assertFalse(reader.advance());
    }

    @Test
    public void testLeadSOAPMany() throws Exception {
        TMarketoInputProperties props = getSOAPProperties();
        props.inputOperation.setValue(getLead);
        props.leadKeyTypeSOAP.setValue(EMAIL);
        props.batchSize.setValue(1);
        props.afterInputOperation();
        String email = EMAIL_LEAD_TEST;
        props.leadKeyValue.setValue(email);
        reader = getReader(props);

        assertTrue(reader.start());
        LOG.debug("record = {}.", reader.getCurrent());
        assertFalse(reader.advance());// getLead sets remaingCount to 0.
    }

    @Test
    public void testLeadSOAPNotFound() throws Exception {
        TMarketoInputProperties props = getSOAPProperties();
        props.inputOperation.setValue(getLead);
        props.leadKeyTypeSOAP.setValue(EMAIL);
        props.batchSize.setValue(1);
        props.afterInputOperation();
        String email = EMAIL_INEXISTANT;
        props.leadKeyValue.setValue(email);
        reader = getReader(props);

        assertFalse(reader.start());
    }

    @Test
    public void testMultipleLeadsSOAP() throws Exception {
        TMarketoInputProperties props = getSOAPProperties();
        props.inputOperation.setValue(getMultipleLeads);
        props.leadSelectorSOAP.setValue(StaticListSelector);
        props.listParam.setValue(STATIC_LIST_NAME);
        props.listParamValue.setValue(UNDX_TEST_LIST_SMALL);
        props.batchSize.setValue(50);
        props.afterInputOperation();
        reader = getReader(props);
        assertTrue(reader.start());
        LOG.debug("record = {}.", reader.getCurrent());
        assertTrue(reader.advance());
        LOG.debug("record = {}.", reader.getCurrent());
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
            // LOG.debug("email: {}", reader.getCurrent().get(1));
        }
        assertTrue(((int) reader.getReturnValues().get(RETURN_NB_CALL)) > 1);
    }

    @Test
    public void testLeadActivitySOAP() throws Exception {
        TMarketoInputProperties props = getSOAPProperties();
        props.inputOperation.setValue(getLeadActivity);
        props.leadKeyTypeSOAP.setValue(EMAIL);
        props.leadSelectorSOAP.setValue(LeadKeySelector);
        props.afterInputOperation();
        props.batchSize.setValue(5);
        //
        props.leadKeyValue.setValue(EMAIL_LEAD_MANY_INFOS);

        reader = getReader(props);
        assertTrue(reader.start());
        LOG.debug("record = {}.", reader.getCurrent());
        assertTrue(reader.advance());
        LOG.debug("record = {}.", reader.getCurrent());
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
            // LOG.debug("email: {}", reader.getCurrent().get(1));
        }
        assertTrue(((int) reader.getReturnValues().get(RETURN_NB_CALL)) > 1);
    }

    @Test
    public void testLeadChanges() throws Exception {
        TMarketoInputProperties props = getSOAPProperties();
        props.inputOperation.setValue(getLeadChanges);
        props.afterInputOperation();
        props.batchSize.setValue(50);
        //
        props.oldestCreateDate.setValue(DATE_OLDEST_CREATE);
        props.latestCreateDate.setValue(DATE_LATEST_CREATE);
        reader = getReader(props);
        assertTrue(reader.start());
        LOG.debug("record = {}.", reader.getCurrent());
        assertTrue(reader.advance());
        LOG.debug("record = {}.", reader.getCurrent());
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
            // LOG.debug("email: {}", reader.getCurrent().get(1));
        }
        assertTrue(((int) reader.getReturnValues().get(RETURN_NB_CALL)) > 1);
    }

    @Test
    public void testLeadDynamicSchema() throws Exception {
        TMarketoInputProperties props = getRESTProperties();
        props.inputOperation.setValue(getLead);
        props.leadKeyTypeREST.setValue(LeadKeyTypeREST.email);
        props.batchSize.setValue(1);
        props.afterInputOperation();
        String email = "undx@undx.net";
        props.leadKeyValue.setValue(email);
        props.schemaInput.schema.setValue(
                SchemaBuilder.builder().record("test").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields().endRecord());
        reader = getReader(props);
        assertTrue(reader.start());
        IndexedRecord r = reader.getCurrent();
        assertNotNull(r);
        assertTrue(r.getSchema().getFields().size() > 6);
        assertFalse(reader.advance());
    }

    @Test
    public void testCustomObjectDynamicSchema() throws Exception {
        TMarketoInputProperties props = getRESTProperties();
        String coName = "smartphone_c";
        String brand = "Apple";
        String models = "iPhone 7";
        props.inputOperation.setValue(CustomObject);
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.batchSize.setValue(1);
        props.afterCustomObjectAction();
        props.customObjectName.setValue(coName);
        props.customObjectFilterType.setValue("model");
        props.customObjectFilterValues.setValue(models);
        Schema design = SchemaBuilder.builder().record("test").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields()
                .endRecord();
        design.addProp(SchemaConstants.INCLUDE_ALL_FIELDS, "true");
        props.schemaInput.schema.setValue(design);
        reader = getReader(props);
        assertTrue(reader.start());
        IndexedRecord r = reader.getCurrent();
        assertNotNull(r);
        assertTrue(r.getSchema().getFields().size() > 6);
        assertFalse(reader.advance());
    }

    @Test
    public void testLeadActivityREST() throws Exception {
        TMarketoInputProperties props = getRESTProperties();
        props.inputOperation.setValue(getLeadActivity);
        String since = new SimpleDateFormat(DATETIME_PATTERN_PARAM).format(new Date(new Date().getTime() - 10000000));
        props.sinceDateTime.setValue(since);
        props.batchSize.setValue(300);
        props.afterInputOperation();
        props.beforeMappingInput();
        reader = getReader(props);
        assertTrue(reader.start());
        IndexedRecord r = reader.getCurrent();
        assertNotNull(r);
        assertTrue(r.getSchema().getFields().size() > 6);
        while (reader.advance()) {
            r = reader.getCurrent();
            assertNotNull(r);
        }
        assertTrue(((int) reader.getReturnValues().get(RETURN_NB_CALL)) >= 6); // 62 activities (62/10 API Limit)
        // test pagination
        props.batchSize.setValue(5);
        reader = getReader(props);
        assertTrue(reader.start());
        r = reader.getCurrent();
        assertNotNull(r);
        assertTrue(r.getSchema().getFields().size() > 6);
        while (reader.advance()) {
            r = reader.getCurrent();
            assertNotNull(r);
        }
        assertTrue(((int) reader.getReturnValues().get(RETURN_NB_CALL)) >= 20);
    }

    @Test
    public void testLeadActivityExcludeREST() throws Exception {
        TMarketoInputProperties props = getRESTProperties();
        props.inputOperation.setValue(getLeadActivity);
        String since = new SimpleDateFormat(DATETIME_PATTERN_PARAM).format(new Date(new Date().getTime() - 10000000));
        props.sinceDateTime.setValue(since);
        props.batchSize.setValue(300);
        props.afterInputOperation();
        props.beforeMappingInput();
        props.setExcludeTypes.setValue(true);
        props.excludeTypes.type.setValue(Arrays.asList("NewLead", "ChangeDataValue"));
        reader = getReader(props);
        assertTrue(reader.start());
        IndexedRecord r = reader.getCurrent();
        assertNotNull(r);
        assertTrue(r.getSchema().getFields().size() > 6);
        while (reader.advance()) {
            r = reader.getCurrent();
            assertNotNull(r);
            assertNotEquals(12, r.get(3)); // activityTypeId != NewLead
            assertNotEquals(13, r.get(3)); // activityTypeId != ChangeDataValue
        }
        assertTrue(((int) reader.getReturnValues().get(RETURN_NB_CALL)) >= 6);
    }

    @Test
    public void testLeadActivityWithSpecificActivitiesREST() throws Exception {
        TMarketoInputProperties props = getRESTProperties();
        props.inputOperation.setValue(getLeadActivity);
        String since = new SimpleDateFormat(DATETIME_PATTERN_PARAM).format(new Date(new Date().getTime() - 10000000));
        props.sinceDateTime.setValue(since);
        props.batchSize.setValue(300);
        props.afterInputOperation();
        props.beforeMappingInput();
        props.setIncludeTypes.setValue(true);
        props.includeTypes.type.setValue(Arrays.asList("DeleteLead", "AddToList"));
        reader = getReader(props);
        assertTrue(reader.start());
        IndexedRecord r = reader.getCurrent();
        assertNotNull(r);
        assertTrue(r.getSchema().getFields().size() > 6);
        while (reader.advance()) {
            r = reader.getCurrent();
            assertNotNull(r);
            assertThat(Integer.parseInt(r.get(3).toString()), anyOf(is(24), is(37)));
        }
        assertTrue(((int) reader.getReturnValues().get(RETURN_NB_CALL)) >= 1);
    }

    @Test(expected = IOException.class)
    public void testLeadActivityWithEmptyActivitiesREST() throws Exception {
        TMarketoInputProperties props = getRESTProperties();
        props.inputOperation.setValue(getLeadActivity);
        String since = new SimpleDateFormat(DATETIME_PATTERN_PARAM).format(new Date(new Date().getTime() - 10000000));
        props.sinceDateTime.setValue(since);
        props.batchSize.setValue(300);
        props.afterInputOperation();
        props.beforeMappingInput();
        props.setIncludeTypes.setValue(true);
        // props.includeTypes.type.setValue(Arrays.asList());
        reader = getReader(props);
        assertTrue(reader.start());
        fail("Shouldn't be here");
    }

    @Test(expected = IOException.class)
    public void testLeadActivityFailSOAP() throws Exception {
        TMarketoInputProperties props = getSOAPProperties();
        props.inputOperation.setValue(getLeadActivity);
        props.leadKeyTypeSOAP.setValue(IDNUM);
        props.leadSelectorSOAP.setValue(LeadKeySelector);
        props.afterInputOperation();
        props.batchSize.setValue(5);
        //
        props.leadKeyValue.setValue("4218473");
        reader = getReader(props);
        reader.start();
        fail("Should not be here");
    }

}
