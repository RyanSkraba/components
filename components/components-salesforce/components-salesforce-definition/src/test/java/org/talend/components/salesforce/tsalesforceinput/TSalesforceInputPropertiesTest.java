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

package org.talend.components.salesforce.tsalesforceinput;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties.QueryMode;
import org.talend.daikon.properties.presentation.Form;

/**
 * Unit tests for {@link TSalesforceInputProperties}
 *
 * @author maksym.basiuk
 */

public class TSalesforceInputPropertiesTest {

    private TSalesforceInputProperties properties;

    @Before
    public void setupInstance() {
        properties = new TSalesforceInputProperties("tSalesforceInputProperties");
    }

    @Test
    public void testSetupProperties() {
        // Check if properties were not set before
        Assert.assertNull(properties.batchSize.getValue());
        Assert.assertNotEquals(QueryMode.Query, properties.queryMode.getValue());
        Assert.assertNull(properties.normalizeDelimiter.getValue());
        Assert.assertNull(properties.columnNameDelimiter.getValue());
        Assert.assertFalse(StringUtils.isNotEmpty(properties.query.getValue()));

        properties.setupProperties();

        // Check if properties were set correctly.
        Assert.assertNotNull(properties.batchSize.getValue());
        Assert.assertEquals(QueryMode.Query, properties.queryMode.getValue());
        Assert.assertNotNull(properties.normalizeDelimiter.getValue());
        Assert.assertNotNull(properties.columnNameDelimiter.getValue());
        Assert.assertTrue(StringUtils.isNotEmpty(properties.query.getValue()));
    }

    @Test
    public void testSetupLayout() {
        // Check if layout was set before
        Form mainForm = properties.getForm(Form.MAIN);
        Assert.assertNull(mainForm);
        Form advancedForm = properties.getForm(Form.ADVANCED);
        Assert.assertNull(advancedForm);

        setupProperties();
        properties.setupLayout();

        // check the result of setup layout
        mainForm = properties.getForm(Form.MAIN);
        advancedForm = properties.getForm(Form.ADVANCED);
        Assert.assertNotNull(mainForm);
        Assert.assertNotNull(advancedForm);
    }

    @Test
    public void testRefreshLayout() {
        setupProperties();
        properties.setupLayout();

        properties.queryMode.setValue(QueryMode.Query);
        properties.manualQuery.setValue(true);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.includeDeleted.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.query.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.condition.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.guessSchema.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.guessQuery.getName()).isHidden());

        properties.refreshLayout(properties.getForm(Form.ADVANCED));
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.normalizeDelimiter.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.columnNameDelimiter.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.batchSize.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.pkChunking.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.chunkSize.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getChildForm(properties.connection.getName())
                .getWidget(properties.connection.bulkConnection.getName()).isHidden());

        properties.queryMode.setValue(QueryMode.Bulk);
        properties.manualQuery.setValue(false);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.includeDeleted.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.query.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.MAIN).getWidget(properties.condition.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.guessSchema.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.MAIN).getWidget(properties.guessQuery.getName()).isHidden());

        properties.pkChunking.setValue(true);
        properties.refreshLayout(properties.getForm(Form.ADVANCED));
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.normalizeDelimiter.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.columnNameDelimiter.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.batchSize.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.pkChunking.getName()).isHidden());
        Assert.assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.chunkSize.getName()).isHidden());
        Assert.assertTrue(properties.getForm(Form.ADVANCED).getChildForm(properties.connection.getName())
                .getWidget(properties.connection.bulkConnection.getName()).isHidden());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsOutputConnection() {
        Assert.assertEquals(1, properties.getAllSchemaPropertiesConnectors(true).size());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsInputConnection() {
        Assert.assertEquals(0, properties.getAllSchemaPropertiesConnectors(false).size());
    }

    private void setupProperties() {
        //Initializing all inner properties
        properties.setupProperties();
        properties.connection.init();
        properties.module.init();
    }

}
