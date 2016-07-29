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
package org.talend.components.datastewardship.tdatastewardshiptaskoutput;

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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class TDataStewardshipTaskOutputPropertiesTest {

    @Inject
    private ComponentService componentService;

    @Test
    public void testTDataStewardshipTaskOutputProperties() {
        TDataStewardshipTaskOutputProperties properties = (TDataStewardshipTaskOutputProperties) componentService
                .getComponentProperties("tDataStewardshipTaskOutput"); //$NON-NLS-1$
        PropertyPathConnector connector = new PropertyPathConnector(Connector.MAIN_NAME, "schema"); //$NON-NLS-1$

        Assert.assertNotNull(properties.schema);
        Assert.assertEquals(Collections.emptySet(), properties.getAllSchemaPropertiesConnectors(true));
        Assert.assertEquals(Collections.singleton(connector), properties.getAllSchemaPropertiesConnectors(false));
    }
}
