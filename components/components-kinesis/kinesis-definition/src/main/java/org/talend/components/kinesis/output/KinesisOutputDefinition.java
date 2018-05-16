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

package org.talend.components.kinesis.output;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.SupportedProduct;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.kinesis.KinesisComponentFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class KinesisOutputDefinition extends AbstractComponentDefinition {

    public static final String NAME = KinesisComponentFamilyDefinition.NAME + "Output";

    public static final String RUNTIME = "org.talend.components.kinesis.runtime.KinesisOutputRuntime";

    public KinesisOutputDefinition() {
        super(NAME, ExecutionEngine.BEAM);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return KinesisOutputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology topology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(topology);
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/kinesis-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "kinesis-runtime"),
                    RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }

    @Override
    public String getIconKey() {
        return "aws-kinesis";
    }

    @Override
    public String[] getFamilies() {
        return new String[] { KinesisComponentFamilyDefinition.NAME };
    }

    @Override
    public List<String> getSupportedProducts() {
        return Arrays.asList(SupportedProduct.DATASTREAMS);
    }
}
