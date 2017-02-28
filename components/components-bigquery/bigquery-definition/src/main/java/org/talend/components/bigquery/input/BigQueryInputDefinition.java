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

package org.talend.components.bigquery.input;

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
import org.talend.components.bigquery.BigQueryComponentFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;


public class BigQueryInputDefinition extends AbstractComponentDefinition {

    public static final String NAME = BigQueryComponentFamilyDefinition.NAME + "Input";

    public static final String RUNTIME = "org.talend.components.bigquery.runtime.BigQueryInputRuntime";

    public BigQueryInputDefinition() {
        super(NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return BigQueryInputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/bigquery-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "bigquery-runtime"), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    public Property[] getReturnProperties() {
        return new Property[]{};
    }

    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public String getIconKey() {
        return "bigquery";
    }
}
