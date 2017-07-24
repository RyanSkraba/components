package ${package}.definition.${componentNameLowerCase};

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

import ${packageTalend}.api.component.AbstractComponentDefinition;
import ${packageTalend}.api.component.ConnectorTopology;
import ${packageTalend}.api.component.runtime.DependenciesReader;
import ${packageTalend}.api.component.runtime.ExecutionEngine;
import ${packageTalend}.api.component.runtime.JarRuntimeInfo;
import ${packageTalend}.api.exception.ComponentException;
import ${packageTalend}.api.properties.ComponentProperties;
import ${packageTalend}.processing.definition.ProcessingFamilyDefinition;
import ${packageDaikon}.properties.property.Property;
import ${packageDaikon}.runtime.RuntimeInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

public class ${componentNameClass}Definition extends AbstractComponentDefinition{

    public static final String NAME = "${componentNameClass}";

    public ${componentNameClass}Definition() {
        super(NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Class<${componentNameClass}Properties> getPropertyClass() {
        return ${componentNameClass}Properties.class;
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
        return "${componentNameLowerCase}";
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/processing-runtime"),
                    DependenciesReader.computeDependenciesFilePath(ProcessingFamilyDefinition.MAVEN_GROUP_ID,
                            ProcessingFamilyDefinition.MAVEN_ARTIFACT_ID),
                    "org.talend.components.processing.runtime.${componentNameClass}Runtime");
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING_AND_OUTGOING);
    }
}
