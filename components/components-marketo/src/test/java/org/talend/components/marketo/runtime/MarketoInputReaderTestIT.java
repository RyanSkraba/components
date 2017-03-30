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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.SOAP;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.CustomObject;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getLead;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getLeadActivity;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getLeadChanges;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.getMultipleLeads;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP.EMAIL;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LeadKeySelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.StaticListSelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam.STATIC_LIST_NAME;

import java.util.ArrayList;

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
        props.connection.apiMode.setValue(SOAP);
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
        props.batchSize.setValue(10);
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
        props.batchSize.setValue(5);
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

}
