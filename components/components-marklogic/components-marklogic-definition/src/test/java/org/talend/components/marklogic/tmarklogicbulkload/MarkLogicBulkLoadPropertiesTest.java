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
package org.talend.components.marklogic.tmarklogicbulkload;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MarkLogicBulkLoadPropertiesTest {

    MarkLogicBulkLoadProperties bulkLoadProperties;

    @Before
    public void setUp() {
        bulkLoadProperties = new MarkLogicBulkLoadProperties("bulk load");
    }

    @Test
    public void testSetupProperties() {
        bulkLoadProperties.setupProperties();

        assertNotNull(bulkLoadProperties.connection);

        assertNotNull(bulkLoadProperties.loadFolder);
        assertNull(bulkLoadProperties.loadFolder.getStringValue());
        assertTrue(bulkLoadProperties.loadFolder.isRequired());

        assertNotNull(bulkLoadProperties.docidPrefix);
        assertNull(bulkLoadProperties.docidPrefix.getStringValue());

        assertNotNull(bulkLoadProperties.mlcpParams);
        assertNull(bulkLoadProperties.mlcpParams.getStringValue());
    }

    @Test
    public void testSetupLayout() {
        bulkLoadProperties.init();

        Form mainForm = bulkLoadProperties.getForm(Form.MAIN);
        assertNotNull(mainForm.getWidget(bulkLoadProperties.connection));
        assertNotNull(mainForm.getChildForm(bulkLoadProperties.connection.getName())
                .getChildForm(bulkLoadProperties.connection.getName()));
    }
}
