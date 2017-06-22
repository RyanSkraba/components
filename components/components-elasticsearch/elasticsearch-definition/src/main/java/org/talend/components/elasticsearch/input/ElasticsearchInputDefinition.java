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

package org.talend.components.elasticsearch.input;

import static org.talend.components.elasticsearch.ElasticsearchComponentFamilyDefinition.MAVEN_GROUP_ID;
import static org.talend.components.elasticsearch.ElasticsearchComponentFamilyDefinition.MAVEN_RUNTIME_ARTIFACT_ID;
import static org.talend.components.elasticsearch.ElasticsearchComponentFamilyDefinition.MAVEN_RUNTIME_URI;

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
import org.talend.components.elasticsearch.ElasticsearchComponentFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class ElasticsearchInputDefinition extends AbstractComponentDefinition {

    public static final String NAME = ElasticsearchComponentFamilyDefinition.NAME + "Input";

    public static final String RUNTIME_2_4 = "org.talend.components.elasticsearch.runtime_2_4.ElasticsearchInputRuntime";

    public ElasticsearchInputDefinition() {
        super(NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return ElasticsearchInputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        try {
            switch (((ElasticsearchInputProperties) properties).getDatasetProperties().getDatastoreProperties().version
                    .getValue()) {
            case V_2_4:
            default:
                return new JarRuntimeInfo(new URL(MAVEN_RUNTIME_URI),
                        DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_RUNTIME_ARTIFACT_ID), RUNTIME_2_4);
            }
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public String getIconKey() {
        return "elastic";
    }
}
