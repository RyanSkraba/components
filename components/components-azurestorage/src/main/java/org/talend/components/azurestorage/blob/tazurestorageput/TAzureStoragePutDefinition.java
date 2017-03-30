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
package org.talend.components.azurestorage.blob.tazurestorageput;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageBlobDefinition;
import org.talend.components.azurestorage.blob.runtime.AzureStoragePutRuntime;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class TAzureStoragePutDefinition extends AzureStorageBlobDefinition {

    public static final String COMPONENT_NAME = "tAzureStoragePut"; //$NON-NLS-1$

    public TAzureStoragePutDefinition() {
        super(COMPONENT_NAME);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[]{RETURN_ERROR_MESSAGE_PROP, RETURN_CONTAINER_PROP,
                RETURN_LOCAL_FOLDER_PROP, RETURN_REMOTE_FOLDER_PROP};
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TAzureStoragePutProperties.class;
    }



    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getPropertiesClass() {
        return TAzureStoragePutProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertConnectorTopologyCompatibility(connectorTopology);
        assertEngineCompatibility(engine);
        return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStoragePutRuntime.class);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }

}
