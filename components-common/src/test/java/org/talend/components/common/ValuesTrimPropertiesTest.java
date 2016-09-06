// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;
import org.talend.daikon.properties.presentation.Form;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class ValuesTrimPropertiesTest {

    @Inject
    protected ComponentService componentService;

    public ValuesTrimPropertiesTest() {
    }

    @Test
    public void testValuesTrimProperties() throws Throwable {
        ValuesTrimPropertis props = (ValuesTrimPropertis) new ValuesTrimPropertis("trim").init();
        Form mainForm = props.getForm(Form.MAIN);
        assertFalse(mainForm.getWidget(props.trimAll.getName()).isHidden());
        assertFalse(mainForm.getWidget(props.trimTable.getName()).isHidden());

        props.trimAll.setValue(true);
        assertTrue(mainForm.getWidget(props.trimAll.getName()).isCallAfter());
        componentService.afterProperty(props.trimAll.getName(), props);
        assertTrue(mainForm.getWidget(props.trimTable.getName()).isHidden());
    }
}
