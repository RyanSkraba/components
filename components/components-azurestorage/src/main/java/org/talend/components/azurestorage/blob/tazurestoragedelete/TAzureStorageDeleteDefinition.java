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
package org.talend.components.azurestorage.blob.tazurestoragedelete;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageBlobDefinition;
import org.talend.components.azurestorage.blob.runtime.AzureStorageDeleteRuntime;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class TAzureStorageDeleteDefinition extends AzureStorageBlobDefinition {

    public static final String COMPONENT_NAME = "tAzureStorageDelete"; //$NON-NLS-1$

    public TAzureStorageDeleteDefinition() {
        super(COMPONENT_NAME);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_CONTAINER_PROP };
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TAzureStorageDeleteProperties.class;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class getPropertiesClass() {
        return TAzureStorageDeleteProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertConnectorTopologyCompatibility(connectorTopology);
        assertEngineCompatibility(engine);
        return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageDeleteRuntime.class);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }

}
