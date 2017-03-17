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
package org.talend.components.azurestorage.queue.tazurestoragequeuepurge;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.daikon.properties.property.Property;

public class TAzureStorageQueuePurgeDefinition extends AzureStorageQueueDefinition {

    public static final String COMPONENT_NAME = "tAzureStorageQueuePurge";

    public TAzureStorageQueuePurgeDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TAzureStorageQueuePurgeProperties.class;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_QUEUE_NAME_PROP };
    }
}
