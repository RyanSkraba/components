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
package org.talend.components.processing.definition.limit;

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
import org.talend.components.processing.definition.ProcessingFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class LimitDefinition extends AbstractComponentDefinition {

    private static final String COMPONENT_NAME = "Limit";

    public LimitDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Class<LimitProperties> getPropertyClass() {
        return LimitProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { ProcessingFamilyDefinition.NAME };
    }

    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    @Override
    public String getIconKey() {
        return "limit";
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/processing-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-runtime"),
                    "org.talend.components.processing.runtime.limit.LimitRuntime");
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING_AND_OUTGOING);
    }

    @Override
    public boolean isConditionalInputs() {
        return false;
    }
}
