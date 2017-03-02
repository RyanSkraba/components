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
package org.talend.components.azurestorage.queue;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageDefinition;
import org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSink;
import org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSource;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

public abstract class AzureStorageQueueDefinition extends AzureStorageDefinition {

    public static final String RETURN_NB_QUEUE = "numberOfQueues";

    public static final Property<Integer> RETURN_NB_QUEUE_PROP = PropertyFactory.newInteger(RETURN_NB_QUEUE);

    public static final String RETURN_QUEUE_NAME = "queueName";

    public static final Property<String> RETURN_QUEUE_NAME_PROP = PropertyFactory.newString(RETURN_QUEUE_NAME);

    public AzureStorageQueueDefinition(String componentName) {
        super(componentName);

        setupI18N(new Property<?>[] { RETURN_NB_QUEUE_PROP, RETURN_QUEUE_NAME_PROP });
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Cloud/Azure Storage/Queue" };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        if (connectorTopology == ConnectorTopology.OUTGOING || connectorTopology == ConnectorTopology.NONE) {
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageQueueSource.class);
        } else {
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageQueueSink.class);
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return AzureStorageQueueProperties.class;
    }

    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { AzureStorageQueueProperties.class });
    }

}
