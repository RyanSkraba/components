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
package org.talend.components.azurestorage.blob.tazurestoragecontainercreate;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.runtime.AzureStorageContainerCreateRuntime;
import org.talend.daikon.runtime.RuntimeInfo;

public class TAzureStorageContainerCreateDefinition extends AzureStorageContainerDefinition {

    public static final String COMPONENT_NAME = "tAzureStorageContainerCreate"; //$NON-NLS-1$

    public TAzureStorageContainerCreateDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TAzureStorageContainerCreateProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {

        assertConnectorTopologyCompatibility(connectorTopology);
        assertEngineCompatibility(engine);
        return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageContainerCreateRuntime.class);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }

}
