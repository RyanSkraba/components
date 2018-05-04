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
package org.talend.components.marketo.runtime.data;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.data.MarketoDatasetProperties;
import org.talend.components.marketo.data.MarketoDatasetProperties.Operation;
import org.talend.components.marketo.data.MarketoInputProperties;
import org.talend.components.marketo.runtime.MarketoInputReader;
import org.talend.components.marketo.runtime.MarketoRuntimeTestBase;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatasetSourceTest extends MarketoRuntimeTestBase {

    MarketoDatasetSource dsSource;

    MarketoInputProperties properties;

    MarketoDatasetProperties dataset;

    TMarketoConnectionProperties datastore;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoDatasetSourceTest.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        datastore = new TMarketoConnectionProperties("test");
        datastore.init();
        datastore.setupProperties();
        datastore.endpoint.setValue("https://abc.mktorest.com/rest");
        datastore.clientAccessId.setValue("fake");
        datastore.secretKey.setValue("sekret");
        dataset = new MarketoDatasetProperties("test");
        dataset.setupProperties();
        dataset.setupLayout();
        dataset.setDatastoreProperties(datastore);
        properties = new MarketoInputProperties("test");
        properties.setupProperties();
        properties.setupLayout();
        properties.setDatasetProperties(dataset);
        dsSource = new MarketoDatasetSource();
    }

    @Test
    public void testSplitIntoBundles() throws Exception {
        List<? extends BoundedSource> bundles = dsSource.splitIntoBundles(1, null);
        assertEquals(1, bundles.size());
        assertTrue(bundles.get(0) instanceof BoundedSource);
    }

    @Test
    public void testGetEstimatedSizeBytes() throws Exception {
        assertEquals(1, dsSource.getEstimatedSizeBytes(null));
    }

    @Test
    public void testProducesSortedKeys() throws Exception {
        assertFalse(dsSource.producesSortedKeys(null));
    }

    @Test
    public void testCreateReader() throws Exception {
        dataset.operation.setValue(Operation.getLeads);
        dsSource.initialize(null, properties);
        BoundedReader reader = dsSource.createReader(null);
        assertNotNull(reader);
        assertEquals(MarketoInputReader.class, reader.getClass());
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        assertEquals(Collections.emptyList(), dsSource.getSchemaNames(null));
    }

    @Test
    public void testGetEndpointSchema() throws Exception {
        dataset.operation.setValue(Operation.getLeads);
        dsSource.initialize(null, properties);
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), dsSource.getEndpointSchema(null, "leads"));
        dataset.operation.setValue(Operation.getLeadChanges);
        dataset.afterOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadChanges(), dsSource.getEndpointSchema(null, "changes"));
        dataset.operation.setValue(Operation.getLeadActivities);
        dataset.afterOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadActivity(), dsSource.getEndpointSchema(null, "activities"));
        MarketoDatasetProperties spy = spy(dataset);
        doNothing().when(spy).retrieveCustomObjectList();
        doNothing().when(spy).retrieveCustomObjectSchema();
        dsSource.initialize(null, properties);
        spy.operation.setValue(Operation.getCustomObjects);
        spy.afterOperation();
        assertEquals(MarketoConstants.getCustomObjectRecordSchema(), dsSource.getEndpointSchema(null, "customobjects"));
    }

    @Test
    public void testValidate() throws Exception {
        dataset.operation.setValue(Operation.getLeads);
        MarketoDatasetSource spy = spy(dsSource);
        spy.initialize(null, properties);
        doReturn(ValidationResult.OK).when(spy).validateConnection(any(MarketoProvideConnectionProperties.class));
        ValidationResult vr = spy.validate(null);
        assertEquals(Result.ERROR, vr.getStatus());
        properties.leadKeyType.setValue("email");
        properties.leadKeyValue.setValue("john.doe@gmail.com");
        vr = spy.validate(null);
        assertEquals(Result.OK, vr.getStatus());
        dataset.operation.setValue(Operation.getLeadChanges);
        vr = spy.validate(null);
        assertEquals(Result.OK, vr.getStatus());
        dataset.operation.setValue(Operation.getLeadActivities);
        vr = spy.validate(null);
        assertEquals(Result.OK, vr.getStatus());
        properties.sinceDateTime.setValue("2018 01 01");
        vr = spy.validate(null);
        assertEquals(Result.ERROR, vr.getStatus());
        properties.sinceDateTime.setValue("");
        vr = spy.validate(null);
        assertEquals(Result.ERROR, vr.getStatus());
        dataset.operation.setValue(Operation.getCustomObjects);
        vr = spy.validate(null);
        assertEquals(Result.ERROR, vr.getStatus());
        dataset.customObjectName.setValue("smartphone_c");
        vr = spy.validate(null);
        assertEquals(Result.ERROR, vr.getStatus());
        dataset.filterType.setValue("brand");
        vr = spy.validate(null);
        assertEquals(Result.ERROR, vr.getStatus());
        dataset.filterValue.setValue("apple");
        vr = spy.validate(null);
        assertEquals(Result.OK, vr.getStatus());
        doReturn(new ValidationResult(Result.ERROR)).when(spy).validateConnection(any(MarketoProvideConnectionProperties.class));
        vr = spy.validate(null);
        assertEquals(Result.ERROR, vr.getStatus());
    }

    @Test
    public void testInitialize() throws Exception {
        ValidationResult res = dsSource.initialize(null, properties);
        assertEquals(Result.OK, res.getStatus());
    }

    @Test
    public void testGetInputProperties() throws Exception {
        dsSource.initialize(null, properties);
        assertEquals(InputOperation.getMultipleLeads, dsSource.getInputProperties().inputOperation.getValue());
        dataset.operation.setValue(Operation.getLeadChanges);
        assertEquals(InputOperation.getLeadChanges, dsSource.getInputProperties().inputOperation.getValue());
        dataset.operation.setValue(Operation.getLeadActivities);
        assertEquals(InputOperation.getLeadActivity, dsSource.getInputProperties().inputOperation.getValue());
        dataset.operation.setValue(Operation.getCustomObjects);
        assertEquals(InputOperation.CustomObject, dsSource.getInputProperties().inputOperation.getValue());
    }

}
