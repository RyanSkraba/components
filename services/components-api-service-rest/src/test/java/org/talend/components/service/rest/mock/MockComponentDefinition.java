//==============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================
package org.talend.components.service.rest.mock;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.fullexample.FullExampleProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

import static java.util.Arrays.asList;

/**
 *
 */
public class MockComponentDefinition extends AbstractComponentDefinition {

    private String name;

    private Set<ConnectorTopology> topologies;

    public MockComponentDefinition(String name) {
        super("mock " + name, ExecutionEngine.DI);
        this.name = name;
        this.topologies = new HashSet<>();
    }

    public MockComponentDefinition(String name, ConnectorTopology... topologies) {
        this(name);
        if (topologies != null) {
            this.topologies.addAll(asList(topologies));
        }
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "mock" };
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        return "mock_" + name + "_icon32.png";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return FullExampleProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology topology) {
        assertEngineCompatibility(engine);
        return null;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return topologies;
    }

}
