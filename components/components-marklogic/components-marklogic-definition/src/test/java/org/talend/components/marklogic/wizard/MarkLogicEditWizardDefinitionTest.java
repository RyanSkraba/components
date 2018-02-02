//==============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================

package org.talend.components.marklogic.wizard;

import org.junit.Test;

import static org.junit.Assert.*;

public class MarkLogicEditWizardDefinitionTest {

    @Test
    public void isTopLevelFalseTest() {
        assertFalse(new MarkLogicEditWizardDefinition().isTopLevel());
    }

    @Test
    public void getNameTest() {
        assertEquals(MarkLogicEditWizardDefinition.COMPONENT_WIZARD_NAME, new MarkLogicEditWizardDefinition().getName());
    }
}