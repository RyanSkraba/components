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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageBlobProperties;
import org.talend.components.azurestorage.blob.helpers.RemoteBlob;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobGet;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.daikon.properties.ValidationResult;

/**
 * The AzureStorageSource provides the mechanism to supply data to other components at run-time.
 *
 * Based on the Apache Beam project, the Source mechanism is appropriate to describe distributed and non-distributed
 * data sources and can be adapted to scalable big data execution engines on a cluster, or run locally.
 *
 * This example component describes an input source that is guaranteed to be run in a single JVM (whether on a cluster
 * or locally), so:
 *
 * <ul>
 * <li>the simplified logic for reading is found in the {@link AzureStorageReader}, and</li>
 * </ul>
 */
public class AzureStorageSource extends AzureStorageSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = 8358040916857157407L;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {

        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        return ValidationResult.OK;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public BoundedReader createReader(RuntimeContainer container) {
        //
        // Container operations
        //
        if (properties instanceof TAzureStorageContainerListProperties) {
            TAzureStorageContainerListProperties props = (TAzureStorageContainerListProperties) properties;
            return new AzureStorageContainerListReader(container, this, props);
        }
        //
        if (properties instanceof TAzureStorageListProperties) {
            TAzureStorageListProperties props = (TAzureStorageListProperties) properties;
            return new AzureStorageListReader(container, this, props);
        }

        return null;
    }

    public List<RemoteBlob> getRemoteBlobs() {
        List<RemoteBlob> remoteBlobs = new ArrayList<RemoteBlob>();
        if (!(this.properties instanceof AzureStorageBlobProperties))
            return null;
        AzureStorageBlobProperties p = (AzureStorageBlobProperties) properties;
        for (int idx = 0; idx < p.remoteBlobs.prefix.getValue().size(); idx++) {
            String prefix = (p.remoteBlobs.prefix.getValue().get(idx) != null) ? p.remoteBlobs.prefix.getValue().get(idx) : "";
            Boolean include = (p.remoteBlobs.include.getValue().get(idx) != null) ? p.remoteBlobs.include.getValue().get(idx)
                    : false;
            remoteBlobs.add(new RemoteBlob(prefix, include));
        }
        return remoteBlobs;
    }

    /**
     * TODO - Refactor this redundant method with getRemoteBlobs...
     */
    public List<RemoteBlobGet> getRemoteBlobsGet() {
        List<RemoteBlobGet> remoteBlobs = new ArrayList<RemoteBlobGet>();
        if (!(this.properties instanceof TAzureStorageGetProperties))
            return null;
        TAzureStorageGetProperties p = (TAzureStorageGetProperties) properties;
        for (int idx = 0; idx < p.remoteBlobsGet.prefix.getValue().size(); idx++) {
            String prefix = (p.remoteBlobsGet.prefix.getValue().get(idx) != null) ? p.remoteBlobsGet.prefix.getValue().get(idx)
                    : "";
            Boolean include = (p.remoteBlobsGet.include.getValue().get(idx) != null)
                    ? p.remoteBlobsGet.include.getValue().get(idx) : false;
            Boolean create = (p.remoteBlobsGet.create.getValue().get(idx) != null) ? p.remoteBlobsGet.create.getValue().get(idx)
                    : false;
            remoteBlobs.add(new RemoteBlobGet(prefix, include, create));
        }
        return remoteBlobs;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer container)
            throws Exception {
        return Arrays.asList(this);
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer container) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer container) {
        return false;
    }
}