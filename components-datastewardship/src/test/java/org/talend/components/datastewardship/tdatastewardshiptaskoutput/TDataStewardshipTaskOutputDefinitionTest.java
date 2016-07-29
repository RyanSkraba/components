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
public class TDataStewardshipTaskOutputDefinitionTest {

    @Inject
    private ComponentService componentService;

    @Test
    public void testTDataStewardshipTaskOutputDefinition() {
        TDataStewardshipTaskOutputDefinition outputDefinition = (TDataStewardshipTaskOutputDefinition) componentService
                .getComponentDefinition("tDataStewardshipTaskOutput"); //$NON-NLS-1$
        Assert.assertArrayEquals(new String[] { "Talend Data Stewardship" }, outputDefinition.getFamilies()); //$NON-NLS-1$
        Assert.assertEquals("org.talend.components", outputDefinition.getMavenGroupId()); //$NON-NLS-1$
        Assert.assertEquals("components-datastewardship", outputDefinition.getMavenArtifactId()); //$NON-NLS-1$
        Assert.assertTrue(outputDefinition.isSchemaAutoPropagate());
    }

}
