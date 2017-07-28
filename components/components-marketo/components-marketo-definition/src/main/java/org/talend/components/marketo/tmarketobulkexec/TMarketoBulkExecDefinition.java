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
package org.talend.components.marketo.tmarketobulkexec;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marketo.MarketoComponentDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class TMarketoBulkExecDefinition extends MarketoComponentDefinition {

    public static final String COMPONENT_NAME = "tMarketoBulkExec";

    public TMarketoBulkExecDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        return getCommonRuntimeInfo(this.getClass().getClassLoader(), RUNTIME_SOURCE_CLASS);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE, ConnectorTopology.OUTGOING);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TMarketoBulkExecProperties.class;
    }

}
