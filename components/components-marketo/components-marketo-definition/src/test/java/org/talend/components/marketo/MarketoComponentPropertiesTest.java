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
package org.talend.components.marketo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.properties.presentation.Form;

public class MarketoComponentPropertiesTest {

    MarketoComponentProperties props;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoComponentPropertiesTest.class);

    @Before
    public void setUp() throws Exception {
        props = new TMarketoInputProperties("test");
    }

    /**
     * Test method for {@link org.talend.components.marketo.MarketoComponentProperties#setupProperties()}.
     */
    @Test
    public final void testSetupProperties() {
        props.connection.setupProperties();
        props.schemaInput.setupProperties();
        props.setupProperties();
        assertEquals(APIMode.REST, props.connection.apiMode.getValue());
    }

    @Test
    public final void testLayout() {
        props.connection.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.setupProperties();
        props.setupLayout();
        List<Form> forms = props.getForms();
        assertEquals(2, forms.size());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.connection.getName()).isVisible());
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(props.getForm(Form.MAIN).getWidget(props.connection.getName()).isVisible());
        assertTrue(props.isApiREST());
        assertFalse(props.isApiSOAP());
    }

}
