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

import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public abstract class AzureStorageBlobDefinition extends AzureStorageContainerDefinition {

    public static final String RETURN_CURRENT_BLOB = "currentBlob"; //$NON-NLS-1$

    public static final String RETURN_LOCAL_FOLDER = "localFolder"; //$NON-NLS-1$

    public static final String RETURN_REMOTE_FOLDER = "remoteFolder"; //$NON-NLS-1$

    public static final Property<String> RETURN_CURRENT_BLOB_PROP = PropertyFactory.newString(RETURN_CURRENT_BLOB);

    public static final Property<String> RETURN_LOCAL_FOLDER_PROP = PropertyFactory.newString(RETURN_LOCAL_FOLDER);

    public static final Property<String> RETURN_REMOTE_FOLDER_PROP = PropertyFactory.newString(RETURN_REMOTE_FOLDER);

    public AzureStorageBlobDefinition(String componentName) {
        super(componentName);
        setupI18N(new Property<?>[] { RETURN_CURRENT_BLOB_PROP, RETURN_LOCAL_FOLDER_PROP, RETURN_REMOTE_FOLDER_PROP });
    }
}
