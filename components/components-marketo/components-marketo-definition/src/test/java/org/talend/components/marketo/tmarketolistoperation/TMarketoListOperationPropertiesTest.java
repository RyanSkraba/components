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
package org.talend.components.marketo.tmarketolistoperation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoTestBase;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties.ListOperation;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class TMarketoListOperationPropertiesTest extends MarketoTestBase {

    TMarketoListOperationProperties props;

    private transient static final Logger LOG = LoggerFactory.getLogger(TMarketoListOperationPropertiesTest.class);

    @Before
    public void setUp() throws Exception {
        props = new TMarketoListOperationProperties("test");
        props.connection.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.setupProperties();
        props.setupLayout();
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        Set<PropertyPathConnector> connectors = new HashSet<>();
        assertEquals(Collections.singleton(props.MAIN_CONNECTOR), props.getAllSchemaPropertiesConnectors(false));
        connectors.add(props.REJECT_CONNECTOR);
        connectors.add(props.FLOW_CONNECTOR);
        assertEquals(connectors, props.getAllSchemaPropertiesConnectors(true));
    }

    @Test
    public void testRefreshLayout() throws Exception {
        Form f = props.getForm(Form.MAIN);
        props.refreshLayout(f);
        props.afterListOperation();
        assertTrue(f.getWidget(props.multipleOperation.getName()).isVisible());
        props.listOperation.setValue(ListOperation.isMemberOf);
        props.afterListOperation();
        assertFalse(f.getWidget(props.multipleOperation.getName()).isVisible());
    }

    @Test
    public void testAfterApiMode() throws Exception {
        props.afterListOperation();
        assertEquals(MarketoConstants.getListOperationRESTSchema(), props.schemaInput.schema.getValue());
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.refreshLayout(props.getForm(Form.MAIN));
        props.afterListOperation();
        assertEquals(MarketoConstants.getListOperationSOAPSchema(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testUpdateOutputSchemas() throws Exception {
        Schema s;
        int mainFieldsCount = props.schemaInput.schema.getValue().getFields().size();
        // REST
        props.updateOutputSchemas();
        s = props.schemaFlow.schema.getValue();
        assertEquals(MarketoConstants.getListOperationFlowRESTSchema(), s);

        s = props.schemaReject.schema.getValue();
        assertEquals(MarketoConstants.getListOperationRejectRESTSchema(), s);
        // SOAP
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.afterListOperation();
        props.updateOutputSchemas();
        mainFieldsCount = props.schemaInput.schema.getValue().getFields().size();
        s = props.schemaFlow.schema.getValue();
        assertEquals(MarketoConstants.getListOperationFlowSOAPSchema(), s);
        assertNotNull(s.getField("Success"));
        assertTrue(s.getField("Success").schema().getTypes().get(0).getType().equals(Schema.Type.BOOLEAN));

        s = props.schemaReject.schema.getValue();
        assertEquals(MarketoConstants.getListOperationRejectSOAPSchema(), s);
        assertNotNull(s.getField("ERROR_MSG"));
        assertTrue(s.getField("ERROR_MSG").schema().getTypes().get(0).getType().equals(Schema.Type.STRING));

        props.listOperation.setValue(ListOperation.removeFrom);
        props.multipleOperation.setValue(true);
        props.afterMultipleOperation();
        assertEquals(props.schemaInput.schema.getValue().getFields().size(),
                props.schemaFlow.schema.getValue().getFields().size());
    }

    @Test
    public void testGetRESTSchemaMain() throws Exception {
        Schema s = MarketoConstants.getListOperationRESTSchema();
        assertEquals(2, s.getFields().size());
    }

    @Test
    public void testGetSOAPSchemaMain() throws Exception {
        Schema s = MarketoConstants.getListOperationSOAPSchema();
        assertEquals(4, s.getFields().size());
    }

    @Test
    public void testAfterSchema() {
        props.schemaListener.afterSchema();
    }

    @Test
    public void testValidation() throws Exception {
        props.listOperation.setValue(ListOperation.isMemberOf);
        props.multipleOperation.setValue(true);
        assertEquals(Result.ERROR, props.validateMultipleOperation().getStatus());
        props.multipleOperation.setValue(false);
        assertEquals(Result.OK, props.validateMultipleOperation().getStatus());
        props.listOperation.setValue(ListOperation.removeFrom);
        props.multipleOperation.setValue(true);
        assertEquals(Result.OK, props.validateMultipleOperation().getStatus());
        props.multipleOperation.setValue(false);
        assertEquals(Result.OK, props.validateMultipleOperation().getStatus());
    }

    @Test
    public void testEnums() {
        assertEquals(ListOperation.addTo, ListOperation.valueOf("addTo"));
        assertEquals(ListOperation.isMemberOf, ListOperation.valueOf("isMemberOf"));
        assertEquals(ListOperation.removeFrom, ListOperation.valueOf("removeFrom"));
    }

}
