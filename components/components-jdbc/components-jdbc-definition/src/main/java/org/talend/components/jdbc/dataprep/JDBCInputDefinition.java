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
package org.talend.components.jdbc.dataprep;

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

/**
 * the database input component which work with data store and data set
 * 
 */
public class JDBCInputDefinition extends AbstractComponentDefinition {

    public static final String BEAM_RUNTIME = "org.talend.components.jdbc.runtime.JDBCInputPTransformRuntime";

    public static final String DI_RUNTIME = "org.talend.components.jdbc.runtime.JDBCSource";

    public static String NAME = "DataPrepDBInput";

    public JDBCInputDefinition() {
        super(NAME, ExecutionEngine.DI, ExecutionEngine.BEAM);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return JDBCInputProperties.class;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public String getIconKey() {
        return "db-input";
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        switch (engine) {
        case BEAM:
            return new JdbcRuntimeInfo((JDBCInputProperties) properties, JDBCFamilyDefinition.getBeamRuntimeMavenURI(),
                    DependenciesReader.computeDependenciesFilePath(JDBCFamilyDefinition.getBeamRuntimeGroupId(),
                            JDBCFamilyDefinition.getBeamRuntimeArtifactId()),
                    BEAM_RUNTIME);
        case DI:
        default:
            return new JdbcRuntimeInfo((JDBCInputProperties) properties, JDBCFamilyDefinition.getDIRuntimeMavenURI(),
                    DependenciesReader.computeDependenciesFilePath(JDBCFamilyDefinition.getDIRuntimeGroupId(),
                            JDBCFamilyDefinition.getDIRuntimeArtifactId()),
                    DI_RUNTIME);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }
}
