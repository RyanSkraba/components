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

package org.talend.components.netsuite.output;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.netsuite.NetSuiteComponentDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Definition of NetSuite Output component.
 *
 *<p>Output component provides following functionality:
 * <ul>
 *     <li>Add record(s)</li>
 *     <li>Update record(s)</li>
 *     <li>Upsert (Update or Add) record(s)</li>
 *     <li>Delete record(s)</li>
 * </ul>
 */
public class NetSuiteOutputDefinition extends NetSuiteComponentDefinition {

    public static final String COMPONENT_NAME = "tNetsuiteOutput"; //$NON-NLS-1$

    public NetSuiteOutputDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI);
    }

    @Override
    public boolean isStartable() {
        return false;
    }

    /**
     * Indicate that a runner of component should check availability of records
     * in outgoing flow(s).
     *
     * @return true
     */
    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        if (connectorTopology != ConnectorTopology.NONE) {
            assertConnectorTopologyCompatibility(connectorTopology);
        }
        NetSuiteOutputProperties outputProperties = (NetSuiteOutputProperties) properties;
        return getRuntimeInfo(outputProperties, NetSuiteComponentDefinition.SINK_CLASS);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { NetSuiteOutputModuleProperties.class });
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return NetSuiteOutputProperties.class;
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP,
                RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_REJECT_RECORD_COUNT_PROP };
    }

}
