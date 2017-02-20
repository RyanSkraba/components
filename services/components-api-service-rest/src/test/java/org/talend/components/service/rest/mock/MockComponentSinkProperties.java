// ==============================================================================
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
// ==============================================================================
package org.talend.components.service.rest.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;

/**
 * Mock source component properties for tests.
 */
public class MockComponentSinkProperties extends MockComponentProperties {

    /**
     * Default constructor.
     *
     * @param name the properties name.
     */
    public MockComponentSinkProperties(String name) {
        super(name);
    }

    @Override
    public Set<? extends Connector> getPossibleConnectors(boolean isOutgoingConnection) {
        return isOutgoingConnection //
        ? super.getPossibleConnectors(isOutgoingConnection) //
                : new HashSet<>(Arrays.asList(new PropertyPathConnector("REJECT", "schema.main")));
    }
}
