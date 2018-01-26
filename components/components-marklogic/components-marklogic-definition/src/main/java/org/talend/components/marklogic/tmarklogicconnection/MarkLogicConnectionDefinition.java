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
package org.talend.components.marklogic.tmarklogicconnection;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marklogic.MarkLogicFamilyDefinition;
import org.talend.components.marklogic.RuntimeInfoProvider;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.EnumSet;
import java.util.Set;

public class MarkLogicConnectionDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "tMarkLogicConnection"; //$NON-NLS-1$

    public MarkLogicConnectionDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI, ExecutionEngine.BEAM);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB Specifics/MarkLogic", "Big Data/MarkLogic" };
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return MarkLogicConnectionProperties.class;
    }

    @Override
    public Property<?>[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        if (connectorTopology == ConnectorTopology.NONE) {
            return RuntimeInfoProvider
                    .getCommonRuntimeInfo("org.talend.components.marklogic.runtime.TMarkLogicConnectionStandalone");
        } else {
            return null;
        }
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }
}