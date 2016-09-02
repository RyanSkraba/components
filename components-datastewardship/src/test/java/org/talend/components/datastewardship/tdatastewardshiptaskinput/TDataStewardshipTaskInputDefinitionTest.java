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
package org.talend.components.datastewardship.tdatastewardshiptaskinput;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class TDataStewardshipTaskInputDefinitionTest {

    @Inject
    private ComponentService componentService;

    @Test
    public void testTDataStewardshipTaskInputDefinition() {
        TDataStewardshipTaskInputDefinition inputDefinition = (TDataStewardshipTaskInputDefinition) componentService
                .getComponentDefinition("tDataStewardshipTaskInput"); //$NON-NLS-1$
        Assert.assertArrayEquals(new String[] { "Talend Data Stewardship" }, inputDefinition.getFamilies()); //$NON-NLS-1$
        Assert.assertTrue(inputDefinition.isSchemaAutoPropagate());
    }

}
