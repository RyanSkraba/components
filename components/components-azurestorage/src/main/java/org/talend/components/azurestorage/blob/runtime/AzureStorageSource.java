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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageProperties;
import org.talend.components.azurestorage.blob.AzureStorageBlobProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerProperties;
import org.talend.components.azurestorage.blob.helpers.RemoteBlob;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobGet;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteProperties;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.components.azurestorage.blob.tazurestorageput.TAzureStoragePutProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
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
    
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageSource.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (AzureStorageProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public BoundedReader createReader(RuntimeContainer container) {
        //
        // Container operations
        //
        if (properties instanceof TAzureStorageContainerCreateProperties) {
            TAzureStorageContainerCreateProperties props = (TAzureStorageContainerCreateProperties) properties;
            return new AzureStorageContainerCreateReader(container, this, props);
        }
        //
        if (properties instanceof TAzureStorageContainerDeleteProperties) {
            TAzureStorageContainerDeleteProperties props = (TAzureStorageContainerDeleteProperties) properties;
            return new AzureStorageContainerDeleteReader(container, this, props);
        }
        //
        if (properties instanceof TAzureStorageContainerExistProperties) {
            TAzureStorageContainerExistProperties props = (TAzureStorageContainerExistProperties) properties;
            return new AzureStorageContainerExistReader(container, this, props);
        }
        if (properties instanceof TAzureStorageContainerListProperties) {
            TAzureStorageContainerListProperties props = (TAzureStorageContainerListProperties) properties;
            return new AzureStorageContainerListReader(container, this, props);
        }
        //
        if (properties instanceof TAzureStorageListProperties) {
            TAzureStorageListProperties props = (TAzureStorageListProperties) properties;
            return new AzureStorageListReader(container, this, props);
        }
        //
        // CloudBlob operations
        //
        if (properties instanceof TAzureStorageDeleteProperties) {
            TAzureStorageDeleteProperties props = (TAzureStorageDeleteProperties) properties;
            return new AzureStorageDeleteReader(container, this, props);
        }
        //
        if (properties instanceof TAzureStorageGetProperties) {
            TAzureStorageGetProperties props = (TAzureStorageGetProperties) properties;
            return new AzureStorageGetReader(container, this, props);
        }
        //
        if (properties instanceof TAzureStoragePutProperties) {
            TAzureStoragePutProperties props = (TAzureStoragePutProperties) properties;
            return new AzureStoragePutReader(container, this, props);
        }

        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        // checks that account name and key are not empty
        ValidationResult superRes = super.validate(container);
        if (superRes != ValidationResult.OK)
            return superRes;
        // No validation for listing containers
        if (this.properties instanceof TAzureStorageContainerListProperties) {
            return ValidationResult.OK;
        }
        // Checks that container name follows MS naming rules
        if (this.properties instanceof AzureStorageContainerProperties) {
            String cnt = ((AzureStorageContainerProperties) this.properties).container.getValue();
            // not empty
            if (StringUtils.isEmpty(cnt)) {
                ValidationResult vr = new ValidationResult();
                vr.setMessage(messages.getMessage("error.ContainerEmpty")); //$NON-NLS-1$
                vr.setStatus(ValidationResult.Result.ERROR);
                return vr;
            }
            // valid characters 0-9 a-z and -
            if (!StringUtils.isAlphanumeric(cnt.replaceAll("-", ""))) {
                ValidationResult vr = new ValidationResult();
                vr.setMessage(messages.getMessage("error.IncorrectName")); //$NON-NLS-1$
                vr.setStatus(ValidationResult.Result.ERROR);
                return vr;
            }
            // all lowercase
            if (!StringUtils.isAllLowerCase(cnt.replaceAll("-", ""))) {
                ValidationResult vr = new ValidationResult();
                vr.setMessage(messages.getMessage("error.UppercaseName")); //$NON-NLS-1$
                vr.setStatus(ValidationResult.Result.ERROR);
                return vr;
            }
            // length range : 3-63
            int cntl = cnt.length();
            if ((cntl < 3) || (cntl > 63)) {
                ValidationResult vr = new ValidationResult();
                vr.setMessage(messages.getMessage("error.LengthError")); //$NON-NLS-1$
                vr.setStatus(ValidationResult.Result.ERROR);
                return vr;
            }
        }
        // Operations on CloudBlob(s)
        if (this.properties instanceof AzureStorageBlobProperties) {
            // Put operation has different properties
            if (this.properties instanceof TAzureStoragePutProperties) {
                TAzureStoragePutProperties p = (TAzureStoragePutProperties) this.properties;
                String folder = p.localFolder.getValue();
                // checks local folder
                if (!new File(folder).exists()) {
                    ValidationResult vr = new ValidationResult();
                    vr.setMessage(messages.getMessage("error.EmptyLocalFolder")); //$NON-NLS-1$
                    vr.setStatus(ValidationResult.Result.ERROR);
                    return vr;
                }
                // checks file list if set.
                if (p.useFileList.getValue() && p.files.fileMask.getValue().size() == 0) {
                    ValidationResult vr = new ValidationResult();
                    vr.setMessage(messages.getMessage("error.EmptyFileList")); //$NON-NLS-1$
                    vr.setStatus(ValidationResult.Result.ERROR);
                    return vr;
                }
                // everything is OK.
                return ValidationResult.OK;
            }
            // Get operation needs some additional properties
            if (this.properties instanceof TAzureStorageGetProperties) {
                //
                if (((TAzureStorageGetProperties) this.properties).remoteBlobsGet.prefix.getValue().size() == 0) {
                    ValidationResult vr = new ValidationResult();
                    vr.setMessage(messages.getMessage("error.EmptyBlobs")); //$NON-NLS-1$
                    vr.setStatus(ValidationResult.Result.ERROR);
                    return vr;
                }
                String folder = ((TAzureStorageGetProperties) this.properties).localFolder.getValue();
                if (!new File(folder).exists()) {
                    ValidationResult vr = new ValidationResult();
                    vr.setMessage(messages.getMessage("error.NonentityLocal")); //$NON-NLS-1$
                    vr.setStatus(ValidationResult.Result.ERROR);
                    return vr;
                }
                // everything is OK.
                return ValidationResult.OK;
            }
            // We need at least one blob filter
            if (((AzureStorageBlobProperties) this.properties).remoteBlobs.prefix.getValue().size() == 0) {
                ValidationResult vr = new ValidationResult();
                vr.setMessage(messages.getMessage("error.EmptyBlobs")); //$NON-NLS-1$
                vr.setStatus(ValidationResult.Result.ERROR);
                return vr;
            }
        }
        return ValidationResult.OK;
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
