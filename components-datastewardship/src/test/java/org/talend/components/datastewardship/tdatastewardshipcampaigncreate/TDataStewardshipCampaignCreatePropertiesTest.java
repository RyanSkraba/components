// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship.tdatastewardshipcampaigncreate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

@SuppressWarnings("nls")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class TDataStewardshipCampaignCreatePropertiesTest {

    @Inject
    private ComponentService componentService;

    @Test
    public void testTDataStewardshipCampaignCreateProperties() {
        TDataStewardshipCampaignCreateProperties properties = (TDataStewardshipCampaignCreateProperties) componentService
                .getComponentProperties("tDataStewardshipCampaignCreate");
        PropertyPathConnector connector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

        Assert.assertNotNull(properties.schema);
        Assert.assertEquals(Collections.emptySet(), properties.getAllSchemaPropertiesConnectors(true));
        Assert.assertEquals(Collections.singleton(connector), properties.getAllSchemaPropertiesConnectors(false));
    }
    
    /**
     * Checks {@link TDataStewardshipCampaignCreateProperties#setupLayout()}
     */
    @Test
    public void testSetupLayout() {
        TDataStewardshipCampaignCreateProperties properties = (TDataStewardshipCampaignCreateProperties) componentService
                .getComponentProperties("tDataStewardshipCampaignCreate"); //$NON-NLS-1$           
        properties.init();
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(2));

        Widget schemaWidget = main.getWidget("schema"); //$NON-NLS-1$
        assertThat(schemaWidget, notNullValue());
        Widget connectionWidget = main.getWidget("connection"); //$NON-NLS-1$
        assertThat(connectionWidget, notNullValue());       
    }
}
