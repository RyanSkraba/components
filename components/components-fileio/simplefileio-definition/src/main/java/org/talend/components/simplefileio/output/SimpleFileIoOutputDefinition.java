// ============================================================================
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
// ============================================================================

package org.talend.components.simplefileio.output;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.simplefileio.SimpleFileIoComponentFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class SimpleFileIoOutputDefinition extends AbstractComponentDefinition {

    public static final String NAME = SimpleFileIoComponentFamilyDefinition.NAME + "Output";

    public static final String RUNTIME = "org.talend.components.simplefileio.runtime.SimpleFileIoOutputRuntime";

    public SimpleFileIoOutputDefinition() {
        super(NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return SimpleFileIoOutputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ComponentProperties properties, ConnectorTopology ctx) {
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/simplefileio-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "simplefileio-runtime"), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }
}
