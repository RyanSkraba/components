// ============================================================================
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
// ============================================================================
package org.talend.components.marklogic.tmarklogicoutput;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marklogic.AbstractMarkLogicComponentDefinition;
import org.talend.components.marklogic.RuntimeInfoProvider;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class MarkLogicOutputDefinition extends AbstractMarkLogicComponentDefinition {

    public static final String COMPONENT_NAME = "tMarkLogicOutput";

    public MarkLogicOutputDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI, ExecutionEngine.BEAM);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases NoSQL/MarkLogic", "Big Data/MarkLogic" }; //$NON-NLS-1$
    }

    @Override
    public Property<?>[] getReturnProperties() {
        return new Property[] { RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_REJECT_RECORD_COUNT_PROP,
                RETURN_ERROR_MESSAGE_PROP };
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return MarkLogicOutputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        return RuntimeInfoProvider.getCommonRuntimeInfo("org.talend.components.marklogic.runtime.MarkLogicSink");
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING);
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }
}
