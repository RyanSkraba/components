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

/**
 * A connector links two components together. It allows its input component to transmit data to its output component.
 *
 * The two components of a connector will be part of the same subjob.
 */
public class ComponentConnector extends AbstractComponentConnection {

    public enum ConnectorType {
        FLOW,
        MAIN,
        REJECT
    }

    protected ConnectorType type;

    public ComponentConnector(ConnectorType type, int maxInput, int maxOutput) {
        super(maxInput, maxOutput);
        this.type = type;
    }

    public ConnectorType getType() {
        return type;
    }

    public void setType(ConnectorType type) {
        this.type = type;
    }

}
