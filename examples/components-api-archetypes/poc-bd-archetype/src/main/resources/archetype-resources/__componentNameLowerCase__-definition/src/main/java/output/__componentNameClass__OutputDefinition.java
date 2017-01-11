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

package ${package}.output;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import ${packageTalend}.api.component.AbstractComponentDefinition;
import ${packageTalend}.api.component.ConnectorTopology;
import ${packageTalend}.api.component.runtime.DependenciesReader;
import ${packageTalend}.api.component.runtime.ExecutionEngine;
import ${packageTalend}.api.component.runtime.JarRuntimeInfo;
import ${packageTalend}.api.exception.ComponentException;
import ${packageTalend}.api.properties.ComponentProperties;
import ${packageTalend}.${componentNameLowerCase}.${componentNameClass}ComponentFamilyDefinition;
import ${packageDaikon}.properties.property.Property;
import ${packageDaikon}.runtime.RuntimeInfo;

public class ${componentNameClass}OutputDefinition extends AbstractComponentDefinition {

    public static final String NAME = ${componentNameClass}ComponentFamilyDefinition.NAME + "Output";

    public static final String RUNTIME${runtimeVersionConverted} = "org.talend.components.${componentNameLowerCase}.runtime${runtimeVersionConverted}.${componentNameClass}OutputRuntime";

    public ${componentNameClass}OutputDefinition() {
        super(NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return ${componentNameClass}OutputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology ctx){
        assertEngineCompatibility(engine);
        try{
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/${componentNameLowerCase}-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components","${componentNameLowerCase}-runtime"),RUNTIME${runtimeVersionConverted});
        }catch(MalformedURLException e){
            throw new ComponentException(e);
        }
    }

    public Property[] getReturnProperties() {
        return new Property[]{};
    }

    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }
}
