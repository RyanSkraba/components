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
package org.talend.components.localio.rowgenerator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.localio.LocalIOComponentFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class RowGeneratorDefinition extends AbstractComponentDefinition {

    public static final String NAME = "RowGenerator";

    public static final String RUNTIME = "org.talend.components.localio.runtime.rowgenerator.RowGeneratorRuntime";

    public RowGeneratorDefinition() {
        super(NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Class<RowGeneratorProperties> getPropertyClass() {
        return RowGeneratorProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { LocalIOComponentFamilyDefinition.NAME };
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    @Override
    public String getIconKey() {
        return "streams";
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return ConnectorTopology.OUTGOING_ONLY;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        try {
            return new JarRuntimeInfo(new URL(LocalIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_URI),
                    DependenciesReader.computeDependenciesFilePath(LocalIOComponentFamilyDefinition.MAVEN_GROUP_ID,
                            LocalIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID),
                    RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

}
