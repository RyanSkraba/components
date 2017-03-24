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

package org.talend.components.hadoopcluster.configuration.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.hadoopcluster.HadoopClusterFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class HadoopClusterConfigurationInputDefinition extends AbstractComponentDefinition {

    public static final String NAME = HadoopClusterFamilyDefinition.NAME + "ConfigurationInput";

    public static final String RUNTIME_AMBARI = "org.talend.components.hadoopcluster.runtime"
            + ".configuration.input.AmbariConfigurationSource";

    public static final String RUNTIME_CM = "org.talend.components.hadoopcluster.runtime"
            + ".configuration.input.ClouderaManagerConfigurationSource";

    public HadoopClusterConfigurationInputDefinition() {
        super(NAME, ExecutionEngine.DI);
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        try {
            HadoopClusterConfigurationInputProperties inputProperties = (HadoopClusterConfigurationInputProperties) properties;
            switch (inputProperties.clusterManagerType.getValue()) {
            case AMBARI:
                return new JarRuntimeInfo(new URL(HadoopClusterFamilyDefinition.MAVEN_RUNTIME_URI),
                        DependenciesReader.computeDependenciesFilePath(HadoopClusterFamilyDefinition.MAVEN_GROUP_ID,
                                HadoopClusterFamilyDefinition.MAVEN_RUNTIME_ARTIFACT_ID),
                        RUNTIME_AMBARI);
            case CLOUDERA_MANAGER:
                return new JarRuntimeInfo(new URL(HadoopClusterFamilyDefinition.MAVEN_RUNTIME_URI),
                        DependenciesReader.computeDependenciesFilePath(HadoopClusterFamilyDefinition.MAVEN_GROUP_ID,
                                HadoopClusterFamilyDefinition.MAVEN_RUNTIME_ARTIFACT_ID),
                        RUNTIME_CM);
            default:
                throw new ComponentException(new RuntimeException("Do not support"));

            }

        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return HadoopClusterConfigurationInputProperties.class;
    }
}
