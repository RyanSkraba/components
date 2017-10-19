// ==============================================================================
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
// ==============================================================================
package org.talend.components.service.rest.mock;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class MockComponentDefinition extends AbstractComponentDefinition {

    private String name;

    private String iconKey;

    private Set<ConnectorTopology> topologies;

    private Class<? extends ComponentProperties> componentProperties = MockComponentProperties.class;

    public MockComponentDefinition(String name) {
        this(name, null, ExecutionEngine.DI);
    }

    public MockComponentDefinition(String name, ConnectorTopology... topologies) {
        this(name, null, ExecutionEngine.DI, topologies);
    }

    public MockComponentDefinition(String name, ExecutionEngine engine, ConnectorTopology... topologies) {
        this(name, null, engine, topologies);
    }

    public MockComponentDefinition(String name, String iconKey) {
        this(name, iconKey, ExecutionEngine.DI);
    }

    public MockComponentDefinition(String name, String iconKey, ConnectorTopology... topologies) {
        this(name, iconKey, ExecutionEngine.DI, topologies);
    }

    public MockComponentDefinition(String name, String iconKey, ExecutionEngine engine, ConnectorTopology... topologies) {
        super("mock " + name, engine);
        this.name = name;
        this.iconKey = iconKey;
        this.topologies = new HashSet<>();
        if (topologies != null) {
            this.topologies.addAll(asList(topologies));
        }
    }

    // TODO: At this point, we should probably have a fluent interface to configure the mocked class.
    public void setPropertyClass(Class<? extends ComponentProperties> componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Override
    public String getIconKey() {
        return iconKey;
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
        return componentProperties;
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
