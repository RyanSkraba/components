/// ============================================================================
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
package org.talend.components.marketo.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoTestBase;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectSyncAction;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;

public class MarketoComponentWizardBasePropertiesTest extends MarketoTestBase {

    MarketoComponentWizardBaseProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new MarketoComponentWizardBaseProperties("test");
        properties.schemaInput.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        assertNull(properties.getAllSchemaPropertiesConnectors(true));
        assertNull(properties.getAllSchemaPropertiesConnectors(false));
    }

    @Test
    public void testEnums() throws Exception {
        assertEquals(CustomObjectAction.describe, CustomObjectAction.valueOf("describe"));
        assertEquals(CustomObjectAction.get, CustomObjectAction.valueOf("get"));
        assertEquals(CustomObjectAction.list, CustomObjectAction.valueOf("list"));
        assertEquals(CustomObjectSyncAction.createOnly, CustomObjectSyncAction.valueOf("createOnly"));
        assertEquals(CustomObjectSyncAction.createOrUpdate, CustomObjectSyncAction.valueOf("createOrUpdate"));
        assertEquals(CustomObjectSyncAction.updateOnly, CustomObjectSyncAction.valueOf("updateOnly"));
    }

    @Test
    public void testBeforeAfterFormFetchLeadSchema() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            properties.beforeFormPresentFetchLeadSchema();
            assertNotNull(properties.allAvailableleadFields);
            assertFalse(properties.allAvailableleadFields.isEmpty());
            properties.afterFetchLeadSchema();
            assertEquals(fakeAllLeadFields().toArray(), properties.allAvailableleadFields.values().toArray());
            assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), properties.schemaInput.schema.getValue());
            List<NamedThing> selected = new ArrayList<>();
            NamedThing nt1 = new SimpleNamedThing("id", "id");
            NamedThing nt2 = new SimpleNamedThing("sfdcAccountId", "sfdcAccountId");
            selected.add(nt1);
            selected.add(nt2);
            properties.selectedLeadColumns.setValue(Arrays.asList(nt1, nt2));
            properties.afterFetchLeadSchema();
            assertEquals("selectedLeadFields", properties.schemaInput.schema.getValue().getName());
            assertEquals(2, properties.schemaInput.schema.getValue().getFields().size());
        }
    }

}
