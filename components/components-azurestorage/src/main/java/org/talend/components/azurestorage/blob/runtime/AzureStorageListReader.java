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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.AzureStorageBlobDefinition;
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.helpers.RemoteBlob;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListDefinition;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.components.common.avro.RootSchemaUtils;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageListReader extends AzureStorageReader<IndexedRecord> {

    private TAzureStorageListProperties properties;

    private Iterator<CloudBlob> blobsIterator;

    private CloudBlob currentBlob;

    private IndexedRecord currentRecord;

    private boolean startable;

    private Boolean advanceable;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageListReader.class);

    /** keep this attribute public for test purpose */
    public AzureStorageBlobService azureStorageBlobService;

    public AzureStorageListReader(RuntimeContainer container, BoundedSource source, TAzureStorageListProperties properties) {
        super(container, source);
        this.properties = properties;
        azureStorageBlobService = new AzureStorageBlobService(
                ((AzureStorageSource) getCurrentSource()).getAzureConnection(container));
    }

    @Override
    public boolean start() throws IOException {
        String mycontainer = properties.container.getValue();
        List<CloudBlob> blobs = new ArrayList<>();
        // build a list with remote blobs to fetch
        List<RemoteBlob> remoteBlobs = ((AzureStorageSource) getCurrentSource()).getRemoteBlobs();
        try {

            for (RemoteBlob rmtb : remoteBlobs) {
                for (ListBlobItem blob : azureStorageBlobService.listBlobs(mycontainer, rmtb.prefix, rmtb.include)) {
                    if (blob instanceof CloudBlob) {
                        blobs.add((CloudBlob) blob);
                    }
                }
            }

            startable = !blobs.isEmpty();
            blobsIterator = blobs.iterator();
        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue()) {
                throw new ComponentException(e);
            }
        }

        if (startable) {
            dataCount++;
            currentBlob = blobsIterator.next();
            IndexedRecord dataRecord = new GenericData.Record(properties.schema.schema.getValue());
            dataRecord.put(0, currentBlob.getName());
            Schema rootSchema = RootSchemaUtils.createRootSchema(properties.schema.schema.getValue(), properties.outOfBandSchema);
            currentRecord = new GenericData.Record(rootSchema);
            currentRecord.put(0, dataRecord);
            currentRecord.put(1, dataRecord);
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        advanceable = blobsIterator.hasNext();
        if (advanceable) {
            dataCount++;
            currentBlob = blobsIterator.next();
            IndexedRecord dataRecord = new GenericData.Record(properties.schema.schema.getValue());
            dataRecord.put(0, currentBlob.getName());
            currentRecord.put(0, dataRecord);
            currentRecord.put(1, dataRecord);
        }

        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() {
        if (!startable || (advanceable != null && !advanceable)) {
            throw new NoSuchElementException();
        }

        if (runtime != null) {
            runtime.setComponentData(runtime.getCurrentComponentId(), AzureStorageBlobDefinition.RETURN_CURRENT_BLOB,
                    currentRecord.get(0));
        }

        return currentRecord;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> resultMap = super.getReturnValues();
        resultMap.put(TAzureStorageContainerListDefinition.RETURN_TOTAL_RECORD_COUNT, dataCount);
        resultMap.put(AzureStorageContainerDefinition.RETURN_CONTAINER, properties.container.getValue());
        if (currentBlob != null) {
            resultMap.put(AzureStorageBlobDefinition.RETURN_CURRENT_BLOB, currentBlob.getName());
        }

        return resultMap;
    }
}
