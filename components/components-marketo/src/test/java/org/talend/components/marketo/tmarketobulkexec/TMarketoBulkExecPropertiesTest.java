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
package org.talend.components.marketo.tmarketobulkexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkFileFormat;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkImportTo;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class TMarketoBulkExecPropertiesTest {

    TMarketoBulkExecProperties props;

    @Before
    public void setUp() throws Exception {
        props = new TMarketoBulkExecProperties("tests");
        props.setupProperties();
        props.connection.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupLayout();
        props.setupLayout();
        props.partitionName.setValue("");
        props.customObjectName.setValue("");
        props.bulkFilePath.setValue("");
        props.logDownloadPath.setValue("");
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        assertEquals(Collections.emptySet(), props.getAllSchemaPropertiesConnectors(false));
        Set<PropertyPathConnector> connectors = new HashSet<>();
        connectors.add(props.FLOW_CONNECTOR);
        assertEquals(connectors, props.getAllSchemaPropertiesConnectors(true));
    }

    @Test
    public void testSetupProperties() throws Exception {
        assertEquals(BulkImportTo.Leads, props.bulkImportTo.getValue());
        assertEquals("", props.bulkFilePath.getValue());
        assertNull(props.listId.getValue());
        assertEquals("", props.partitionName.getValue());
        assertEquals(BulkFileFormat.csv, props.bulkFileFormat.getValue());
    }

    @Test
    public void testSetupLayout() throws Exception {
        Form form = props.getForm(Form.MAIN);
        assertTrue(form.getWidget(props.lookupField).isVisible());
        assertTrue(form.getWidget(props.listId).isVisible());
        assertTrue(form.getWidget(props.partitionName).isVisible());
    }

    @Test
    public void testRefreshLayout() throws Exception {
        Form form = props.getForm(Form.MAIN);
        props.refreshLayout(form);
        assertTrue(form.getWidget(props.lookupField).isVisible());
        assertTrue(form.getWidget(props.listId).isVisible());
        assertTrue(form.getWidget(props.partitionName).isVisible());
        props.bulkImportTo.setValue(BulkImportTo.CustomObjects);
        props.refreshLayout(form);
        assertFalse(form.getWidget(props.lookupField).isVisible());
        assertFalse(form.getWidget(props.listId).isVisible());
        assertFalse(form.getWidget(props.partitionName).isVisible());
        props.refreshLayout(props.connection.getForm(Form.REFERENCE));
    }

    @Test
    public void testAfterBulkImportTo() throws Exception {
        Form form = props.getForm(Form.MAIN);
        assertTrue(form.getWidget(props.lookupField).isVisible());
        assertTrue(form.getWidget(props.listId).isVisible());
        assertTrue(form.getWidget(props.partitionName).isVisible());
        props.bulkImportTo.setValue(BulkImportTo.CustomObjects);
        props.afterBulkImportTo();
        assertFalse(form.getWidget(props.lookupField).isVisible());
        assertFalse(form.getWidget(props.listId).isVisible());
        assertFalse(form.getWidget(props.partitionName).isVisible());
    }

    @Test
    public void testSchema() throws Exception {
        props.afterBulkImportTo();
        assertEquals(MarketoConstants.getBulkImportLeadSchema(), props.schemaInput.schema.getValue());
        props.bulkImportTo.setValue(BulkImportTo.CustomObjects);
        props.afterBulkImportTo();
        assertEquals(MarketoConstants.getBulkImportCustomObjectSchema(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testEnums() throws Exception {
        assertEquals(BulkFileFormat.csv, BulkFileFormat.valueOf("csv"));
        assertEquals(BulkFileFormat.tsv, BulkFileFormat.valueOf("tsv"));
        assertEquals(BulkFileFormat.ssv, BulkFileFormat.valueOf("ssv"));
        assertEquals(BulkImportTo.Leads, BulkImportTo.valueOf("Leads"));
        assertEquals(BulkImportTo.CustomObjects, BulkImportTo.valueOf("CustomObjects"));
    }

    @Test
    public void testValidateBulkImportTo() throws Exception {
        assertEquals(Result.OK, props.validateBulkImportTo().getStatus());
        props.connection.apiMode.setValue(APIMode.SOAP);
        assertEquals(Result.ERROR, props.validateBulkImportTo().getStatus());
    }

}
