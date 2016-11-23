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
package org.talend.components.dataprep.tdatasetinput;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.service.spring.SpringTestApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class TDataSetInputDefinitionAndPropertiesTest {

    @Inject
    private ComponentService componentService;

    @Test
    public void testTDataSetInputDefinition() {
        TDataSetInputDefinition inputDefinition = (TDataSetInputDefinition) componentService
                .getComponentDefinition("tDatasetInput");
        Assert.assertArrayEquals(new String[] { "Talend Data Preparation" }, inputDefinition.getFamilies());
        Assert.assertTrue(inputDefinition.isSchemaAutoPropagate());
    }

    @Test
    public void testTDataSetInputProperties() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.getComponentProperties("tDatasetInput");
        PropertyPathConnector connector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

        Assert.assertNotNull(properties.getSchema());
        Assert.assertEquals(Collections.singleton(connector), properties.getAllSchemaPropertiesConnectors(true));
        Assert.assertEquals(Collections.emptySet(), properties.getAllSchemaPropertiesConnectors(false));
    }
}