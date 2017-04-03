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
package org.talend.components.kafka.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.kafka.KafkaFamilyDefinition;
import org.talend.components.kafka.KafkaIOBasedDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaInputDefinition extends KafkaIOBasedDefinition {

    public static String NAME = "KafkaInput";

    public static String RUNTIME = "org.talend.components.kafka.runtime.KafkaInputPTransformRuntime";

    public KafkaInputDefinition() {
        super(NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return KafkaInputProperties.class;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        try {
            return new JarRuntimeInfo(new URL(KafkaFamilyDefinition.MAVEN_DEFAULT_RUNTIME_URI),
                    DependenciesReader.computeDependenciesFilePath(KafkaFamilyDefinition.MAVEN_GROUP_ID,
                            KafkaFamilyDefinition.MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

}
