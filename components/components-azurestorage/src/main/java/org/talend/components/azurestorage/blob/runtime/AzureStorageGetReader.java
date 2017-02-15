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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.AzureStorageBlobDefinition;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobGet;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageGetReader extends AzureStorageReader<Boolean> {

    private TAzureStorageGetProperties properties;

    private Boolean result = Boolean.FALSE;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageGetReader.class);

    public AzureStorageGetReader(RuntimeContainer container, BoundedSource source, TAzureStorageGetProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        List<RemoteBlobGet> remoteBlobs = ((AzureStorageSource) getCurrentSource()).getRemoteBlobsGet();
        String mycontainer = properties.container.getValue();
        String localFolder = properties.localFolder.getValue();
        try {
            CloudBlobContainer container = ((AzureStorageSource) getCurrentSource()).getStorageContainerReference(runtime,
                    mycontainer);
            for (RemoteBlobGet rmtb : remoteBlobs) {
                for (ListBlobItem blob : container.listBlobs(rmtb.prefix, rmtb.include)) {
                    if (blob instanceof CloudBlob) {
                        // TODO - Action when create is false and include is true ???
                        if (rmtb.create) {
                            new File(localFolder + "/" + ((CloudBlob) blob).getName()).getParentFile().mkdirs();
                        }
                        ((CloudBlob) blob).download(new FileOutputStream(localFolder + "/" + ((CloudBlob) blob).getName()));
                        dataCount++;
                    }
                }
            }
        } catch (StorageException | InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
        }
        result = (dataCount > 0);
        return result;
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
        resultMap.put(AzureStorageContainerDefinition.RETURN_CONTAINER, properties.container.getValue());
        resultMap.put(AzureStorageBlobDefinition.RETURN_LOCAL_FOLDER, properties.localFolder.getValue());

        return resultMap;
    }
}
