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
package org.talend.components.api.component;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.talend.components.api.component.Trigger.TriggerType;

/**
 * created by pbailly on 4 Dec 2015 Detailled comment
 *
 */
public class TriggerTest {

    @Test
    public void testTriggerType() {
        assertEquals(6, TriggerType.values().length);
        List<TriggerType> types = Arrays.asList(TriggerType.ITERATE, TriggerType.SUBJOB_OK, TriggerType.SUBJOB_ERROR,
                TriggerType.COMPONENT_OK, TriggerType.COMPONENT_ERROR, TriggerType.RUN_IF);
        assertEquals(types, Arrays.asList(TriggerType.values()));
    }

    @Test
    public void testBasicComponentTrigger() {
        Trigger Trigger = new Trigger(TriggerType.SUBJOB_OK, 5, 6);
        assertEquals(TriggerType.SUBJOB_OK, Trigger.getType());
        assertEquals(5, Trigger.getMaxInput());
        assertEquals(6, Trigger.getMaxOutput());

        Trigger.setType(TriggerType.RUN_IF);
        assertEquals(TriggerType.RUN_IF, Trigger.getType());
        Trigger.setMaxInput(7);
        assertEquals(7, Trigger.getMaxInput());
        Trigger.setMaxOutput(8);
        assertEquals(8, Trigger.getMaxOutput());
    }
}
