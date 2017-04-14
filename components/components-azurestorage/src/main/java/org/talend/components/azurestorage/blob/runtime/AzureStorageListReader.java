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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.AzureStorageBlobDefinition;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.helpers.RemoteBlob;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListDefinition;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageListReader extends AzureStorageReader<IndexedRecord> {

    private TAzureStorageListProperties properties;

    private List<CloudBlob> blobs = new ArrayList<>();

    private int blobIndex;

    private int blobSize;

    private CloudBlob currentBlob;

    private IndexedRecord currentRecord;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageListReader.class);

    public AzureStorageListReader(RuntimeContainer container, BoundedSource source, TAzureStorageListProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable = false;
        String mycontainer = properties.container.getValue();
        // build a list with remote blobs to fetch
        List<RemoteBlob> remoteBlobs = ((AzureStorageSource) getCurrentSource()).getRemoteBlobs();
        try {
            CloudBlobContainer container = ((AzureStorageSource) getCurrentSource())
                    .getAzureStorageBlobContainerReference(runtime, mycontainer);
            blobs = new ArrayList<>();
            for (RemoteBlob rmtb : remoteBlobs) {
                for (ListBlobItem blob : container.listBlobs(rmtb.prefix, rmtb.include)) {
                    if (blob instanceof CloudBlob) {
                        blobs.add((CloudBlob) blob);
                    }
                }
            }

            startable = !blobs.isEmpty();
        } catch (StorageException | InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
        }
        if (startable) {
            dataCount++;
            blobIndex = 0;
            currentRecord = new GenericData.Record(properties.schema.schema.getValue());
            currentRecord.put(0, blobs.get(blobIndex).getName());
            currentBlob = blobs.get(blobIndex);
            blobSize = blobs.size();
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        blobIndex++;
        if (blobIndex < blobSize) {
            dataCount++;
            currentRecord = new GenericData.Record(properties.schema.schema.getValue());
            currentRecord.put(0, blobs.get(blobIndex).getName());
            currentBlob = blobs.get(blobIndex);
            return true;
        }
        return false;
    }

    @Override
    public IndexedRecord getCurrent() {
        if (runtime != null)
            runtime.setComponentData(runtime.getCurrentComponentId(), AzureStorageBlobDefinition.RETURN_CURRENT_BLOB,
                    currentRecord.get(0));
        return currentRecord;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> resultMap = super.getReturnValues();
        resultMap.put(TAzureStorageContainerListDefinition.RETURN_TOTAL_RECORD_COUNT, dataCount);
        resultMap.put(AzureStorageContainerDefinition.RETURN_CONTAINER, properties.container.getValue());
        if (currentBlob != null)
            resultMap.put(AzureStorageBlobDefinition.RETURN_CURRENT_BLOB, currentBlob.getName());

        return resultMap;
    }
}
