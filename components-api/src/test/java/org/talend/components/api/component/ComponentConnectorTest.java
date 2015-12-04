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
import org.talend.components.api.component.ComponentConnector.ConnectorType;

/**
 * created by pbailly on 4 Dec 2015 Detailled comment
 *
 */
public class ComponentConnectorTest {

    @Test
    public void testConnectorType() {
        assertEquals(3, ConnectorType.values().length);
        List<ConnectorType> types = Arrays.asList(ConnectorType.FLOW, ConnectorType.MAIN, ConnectorType.REJECT);
        assertEquals(types, Arrays.asList(ConnectorType.values()));
    }

    @Test
    public void testBasicComponentConnector() {
        ComponentConnector connector = new ComponentConnector(ConnectorType.MAIN, 5, 6);
        assertEquals(ConnectorType.MAIN, connector.getType());
        assertEquals(5, connector.getMaxInput());
        assertEquals(6, connector.getMaxOutput());

        connector.setType(ConnectorType.REJECT);
        assertEquals(ConnectorType.REJECT, connector.getType());
        connector.setMaxInput(7);
        assertEquals(7, connector.getMaxInput());
        connector.setMaxOutput(8);
        assertEquals(8, connector.getMaxOutput());
    }
}
