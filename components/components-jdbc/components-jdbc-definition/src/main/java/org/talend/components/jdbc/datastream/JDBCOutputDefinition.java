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

package org.talend.components.jdbc.datastream;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.JDBCFamilyDefinition;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class JDBCOutputDefinition extends AbstractComponentDefinition {

    public final static String NAME = "JdbcOutput";

    public final static String BEAM_RUNTIME = "org.talend.components.jdbc.runtime.JDBCOutputPTransformRuntime";

    public JDBCOutputDefinition() {
        super(NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        return new JdbcRuntimeInfo((JDBCOutputProperties) properties, JDBCFamilyDefinition.MAVEN_RUNTIME_BEAM_URI,
                DependenciesReader.computeDependenciesFilePath(JDBCFamilyDefinition.MAVEN_GROUP_ID,
                        JDBCFamilyDefinition.MAVEN_RUNTIME_BEAM_ARTIFACT_ID),
                BEAM_RUNTIME);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return JDBCOutputProperties.class;
    }
}
