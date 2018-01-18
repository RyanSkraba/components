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
package org.talend.components.google.drive.get;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;

public class GoogleDriveGetPropertiesTest {

    GoogleDriveGetProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new GoogleDriveGetProperties("test");
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.schemaMain.setupProperties();
        properties.schemaMain.setupLayout();
        properties.setupProperties();
        properties.setupLayout();
    }

    @Test
    public void testAfterStoreToLocal() throws Exception {
        properties.refreshLayout(properties.getForm(Form.MAIN));
        properties.refreshLayout(properties.getForm(Form.ADVANCED));
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.outputFileName.getName()).isVisible());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.setOutputExt.getName()).isVisible());
        properties.storeToLocal.setValue(true);
        properties.afterStoreToLocal();
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.outputFileName.getName()).isVisible());
        assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.setOutputExt.getName()).isVisible());
    }

}
