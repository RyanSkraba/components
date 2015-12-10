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
package org.talend.components.api.properties;

import static org.junit.Assert.*;

import org.junit.Test;

public class NameAndLabelTest {

    @Test
    public void testNameAndLabel() {
        NameAndLabel nameAndLabel = new NameAndLabel("testName", "testLabel");
        assertEquals("testName", nameAndLabel.getName());
        assertEquals("testLabel", nameAndLabel.getLabel());
        nameAndLabel.setName("newTestName");
        assertEquals("newTestName", nameAndLabel.getName());
        nameAndLabel.setLabel("newTestLabel");
        assertEquals("newTestLabel", nameAndLabel.getLabel());
    }

}
