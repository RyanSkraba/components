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
package org.talend.components.marketo.tmarketoinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;

public class TMarketoInputPropertiesTestIT extends MarketoBaseTestIT {

    private TMarketoInputProperties props;

    @Before
    public final void setup() {
        props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_REST);
        props.connection.clientAccessId.setValue(USERID_REST);
        props.connection.secretKey.setValue(SECRETKEY_REST);
        props.connection.apiMode.setValue(REST);
        props.schemaInput.setupProperties();
        props.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupLayout();
        props.setupLayout();
        props.inputOperation.setValue(InputOperation.CustomObject);
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.refreshLayout(props.getForm(Form.MAIN));
        props.afterInputOperation();
    }

    @Test
    public void testFetchCustomObjectSchema() throws Exception {
        assertEquals(MarketoConstants.getCustomObjectRecordSchema(), props.schemaInput.schema.getValue());
        props.customObjectName.setValue("smartphone_c");
        props.validateFetchCustomObjectSchema();
        props.afterFetchCustomObjectSchema();
        Schema s = props.schemaInput.schema.getValue();
        Field f = s.getField("model");
        assertNotNull(f);
        assertEquals("STRING", f.schema().getType().toString());
        assertEquals("true", f.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        f = s.getField("brand");
        assertNotNull(f);
        assertEquals("STRING", f.schema().getType().toString());
        f = s.getField("customerId");
        assertNotNull(f);
        assertEquals("INT", f.schema().getType().toString());
        f = s.getField("marketoGUID");
        assertNotNull(f);
        assertEquals("STRING", f.schema().getType().toString());
    }

}
