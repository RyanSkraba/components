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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;

import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageContainerListReader extends AzureStorageReader<IndexedRecord> {

    private IndexedRecord currentRecord;

    private TAzureStorageContainerListProperties properties;

    private transient Iterator<CloudBlobContainer> containers;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageContainerListReader.class);

    private Boolean startable = null; // this is initialized in the start method

    Boolean advanceable = null; // this is initialized in the advance method

    /** let this attribute public for test purpose */
    public AzureStorageBlobService blobService;

    public AzureStorageContainerListReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageContainerListProperties properties) {
        super(container, source);
        this.properties = properties;
        AzureStorageSource currentSource = (AzureStorageSource) source;
        this.blobService = new AzureStorageBlobService(currentSource.getAzureConnection(container));
    }

    @Override
    public boolean start() throws IOException {
        startable = false;
        try {
            containers = blobService.listContainers().iterator();
            startable = containers.hasNext();
        } catch (InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
            else
                startable = false;
        }
        if (startable) {
            dataCount++;
            currentRecord = new GenericData.Record(properties.schema.schema.getValue());
            currentRecord.put(0, containers.next().getName());
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        advanceable = containers.hasNext();
        if (advanceable) {
            dataCount++;
            currentRecord = new GenericData.Record(properties.schema.schema.getValue());
            currentRecord.put(0, containers.next().getName());
        }
        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (startable == null || (advanceable != null && !advanceable)) {
            throw new NoSuchElementException();
        }

        return currentRecord;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> resultMap = super.getReturnValues();
        resultMap.put(TAzureStorageContainerListDefinition.RETURN_TOTAL_RECORD_COUNT, dataCount);

        return resultMap;
    }
}
