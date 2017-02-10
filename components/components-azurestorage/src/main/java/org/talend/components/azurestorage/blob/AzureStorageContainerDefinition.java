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
package org.talend.components.azurestorage.blob;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageDefinition;
import org.talend.components.azurestorage.blob.runtime.AzureStorageSource;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Class AzureStorageContainerDefinition.
 */
public abstract class AzureStorageContainerDefinition extends AzureStorageDefinition {

    /** Azure storage container */
    public static final String RETURN_CONTAINER = "container"; //$NON-NLS-1$

    public static final Property<String> RETURN_CONTAINER_PROP = PropertyFactory.newString(RETURN_CONTAINER);

    public AzureStorageContainerDefinition(String componentName) {
        super(componentName);
        setupI18N(new Property<?>[] { RETURN_CONTAINER_PROP });
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Cloud/Azure Storage/Blob" };
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_CONTAINER_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        if (connectorTopology == ConnectorTopology.OUTGOING || connectorTopology == ConnectorTopology.NONE) {
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), AzureStorageSource.class);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { AzureStorageContainerProperties.class });
    }
}
