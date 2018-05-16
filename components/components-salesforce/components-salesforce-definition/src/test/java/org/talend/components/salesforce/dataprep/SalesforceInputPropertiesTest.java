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

package org.talend.components.salesforce.dataprep;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.daikon.properties.presentation.Form;

/**
 *
 */
public class SalesforceInputPropertiesTest extends SalesforceTestBase {

    private SalesforceInputProperties properties;

    private SalesforceDatasetProperties datasetProperties;

    @Before
    public void setUp() {
        datasetProperties = new SalesforceDatasetProperties("dataset");
        datasetProperties.init();
        properties = new SalesforceInputProperties("root");
        properties.setDatasetProperties(datasetProperties);
        properties.init();
    }

    @Test
    public void testSetupProperties() {
        assertEquals(datasetProperties, properties.getDatasetProperties());
    }

    @Test
    public void testSetupLayout() {
        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm);
    }

    @Test
    public void testPropertiesConnectors() {

        assertThat(properties.getPossibleConnectors(true), containsInAnyOrder(
                (Connector) new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main")));

        assertThat(properties.getPossibleConnectors(false), empty());
    }

}
