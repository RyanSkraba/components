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
package org.talend.components.azurestorage.blob.tazurestoragecontainerlist;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.daikon.properties.property.Property;

/**
 * TAzureStorageContainerListDefinition. TODO - Create a palette icon for this new component !!!
 *
 * List the Azure storage containers available for the storage account.
 */
public class TAzureStorageContainerListDefinition extends AzureStorageContainerDefinition {

    public static final String COMPONENT_NAME = "tAzureStorageContainerList"; //$NON-NLS-1$

    public TAzureStorageContainerListDefinition() {
        super(COMPONENT_NAME);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP };
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TAzureStorageContainerListProperties.class;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class getPropertiesClass() {
        return TAzureStorageContainerListProperties.class;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

}
