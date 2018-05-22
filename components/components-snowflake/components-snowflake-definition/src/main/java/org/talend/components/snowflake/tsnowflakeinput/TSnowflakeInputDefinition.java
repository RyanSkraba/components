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
package org.talend.components.snowflake.tsnowflakeinput;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeDefinition;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Component that can connect to a snowflake system and get some data out of it.
 */

public class TSnowflakeInputDefinition extends SnowflakeDefinition {

    public static final String COMPONENT_NAME = "tSnowflakeInput"; //$NON-NLS-1$

    public TSnowflakeInputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSnowflakeInputProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() { // TODO: Check for redundant
        // properties
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[]{SnowflakeTableProperties.class});
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology componentType) {
        assertEngineCompatibility(engine);
        if (componentType == ConnectorTopology.OUTGOING) {
            return getCommonRuntimeInfo(SnowflakeDefinition.SOURCE_CLASS);
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

}
