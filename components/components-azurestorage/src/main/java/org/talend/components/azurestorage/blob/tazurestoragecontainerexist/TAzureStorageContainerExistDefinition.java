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
package org.talend.components.azurestorage.blob.tazurestoragecontainerexist;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.runtime.AzureStorageContainerExistRuntime;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

public class TAzureStorageContainerExistDefinition extends AzureStorageContainerDefinition {

    public static final String COMPONENT_NAME = "tAzureStorageContainerExist"; //$NON-NLS-1$

    public static final String RETURN_CONTAINER_EXIST = "containerExist"; //$NON-NLS-1$

    public static final Property<String> RETURN_CONTAINER_EXIST_PROP = PropertyFactory.newString(RETURN_CONTAINER_EXIST);

    public TAzureStorageContainerExistDefinition() {
        super(COMPONENT_NAME);
        setupI18N(new Property<?>[] { RETURN_CONTAINER_EXIST_PROP });
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_CONTAINER_PROP, RETURN_CONTAINER_EXIST_PROP };
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TAzureStorageContainerExistProperties.class;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class getPropertiesClass() {
        return TAzureStorageContainerExistProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertConnectorTopologyCompatibility(connectorTopology);
        assertEngineCompatibility(engine);
        return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageContainerExistRuntime.class);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }

}
