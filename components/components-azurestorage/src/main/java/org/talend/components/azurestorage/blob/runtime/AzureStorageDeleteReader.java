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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.helpers.RemoteBlob;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageDeleteReader extends AzureStorageReader<Boolean> {

    private TAzureStorageDeleteProperties properties;

    private Boolean result = Boolean.FALSE;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageDeleteReader.class);
    
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageDeleteReader.class);

    public AzureStorageDeleteReader(RuntimeContainer container, BoundedSource source, TAzureStorageDeleteProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        List<RemoteBlob> remoteBlobs = ((AzureStorageSource) getCurrentSource()).getRemoteBlobs();
        String mycontainer = properties.container.getValue();
        try {
            CloudBlobClient clientService = ((AzureStorageSource) getCurrentSource()).getServiceClient(runtime);
            CloudBlobContainer container = clientService.getContainerReference(mycontainer);
            for (RemoteBlob rmtb : remoteBlobs) {
                for (ListBlobItem blob : container.listBlobs(rmtb.prefix, rmtb.include)) {
                    if (blob instanceof CloudBlockBlob) {
                        // FIXME - problem with blobs with space in name...
                        if (((CloudBlockBlob) blob).deleteIfExists()) {
                            dataCount++;
                        } else
                            LOGGER.warn(messages.getMessage("warn.FaildDelete", ((CloudBlockBlob) blob).getName()));
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

        return resultMap;
    }
}
