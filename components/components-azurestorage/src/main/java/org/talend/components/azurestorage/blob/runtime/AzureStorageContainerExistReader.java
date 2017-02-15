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
package org.talend.components.azurestorage.blob.runtime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistProperties;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageContainerExistReader extends AzureStorageReader<Boolean> {

    private transient Boolean result = Boolean.FALSE;

    protected TAzureStorageContainerExistProperties properties;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageContainerExistReader.class);

    protected AzureStorageContainerExistReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageContainerExistProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        try {
            String mycontainer = properties.container.getValue();
            CloudBlobClient clientService = ((AzureStorageSource) getCurrentSource()).getServiceClient(runtime);
            CloudBlobContainer container = clientService.getContainerReference(mycontainer);
            result = container.exists();
            return result;
        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
            return false;
        }
    }

    @Override
    public boolean advance() throws IOException {
        return false;
    }

    @Override
    public Boolean getCurrent() throws NoSuchElementException {
        return result;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> resultMap = super.getReturnValues();
        resultMap.put(TAzureStorageContainerExistDefinition.RETURN_CONTAINER, properties.container.getValue());
        resultMap.put(TAzureStorageContainerExistDefinition.RETURN_CONTAINER_EXIST, new Boolean(result));

        return resultMap;
    }
}
