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
package org.talend.components.azurestorage;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;

public interface AzureConnection {

    /**
     * Return CloudStorageAccount for azure
     * 
     * @throws URISyntaxException
     * @throws InvalidKeyException
     */
    CloudStorageAccount getCloudStorageAccount() throws InvalidKeyException, URISyntaxException;

}
