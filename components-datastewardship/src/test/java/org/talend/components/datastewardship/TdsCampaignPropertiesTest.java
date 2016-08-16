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
package org.talend.components.datastewardship;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TdsCampaignPropertiesTest} class
 */
public class TdsCampaignPropertiesTest {

    /**
     * Checks {@link TdsConnectionProperties#setupLayout()} creates Main form, which contain 3 widgets and checks
     * widgets names <br>
     */
    @Test
    public void testSetupLayout() {
        TdsCampaignProperties properties = new TdsCampaignProperties("root"); //$NON-NLS-1$
        properties.init();
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(3));
        Widget campaignNameWidget = main.getWidget("campaignName"); //$NON-NLS-1$
        assertThat(campaignNameWidget, notNullValue());
        Widget campaignTypeWidget = main.getWidget("campaignType"); //$NON-NLS-1$
        assertThat(campaignTypeWidget, notNullValue());
    }

}
