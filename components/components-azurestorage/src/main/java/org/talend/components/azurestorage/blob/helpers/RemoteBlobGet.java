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
package org.talend.components.azurestorage.blob.helpers;

/**
 * Class RemoteBlobGet.
 *
 * Same as {@link RemoteBlobsTable} for the TAzureStoragePut.
 */
public class RemoteBlobGet extends RemoteBlob {

    /** create - Create parent directories. */
    public Boolean create;

    /**
     * Instantiates a new RemoteBlobGet(String prefix, Boolean include, Boolean create).
     *
     * @param prefix {@link String} prefix
     * @param include {@link Boolean} include
     * @param create {@link Boolean} create
     */
    public RemoteBlobGet(String prefix, Boolean include, Boolean create) {
        super(prefix, include);
        this.create = create;
    }

    /**
     * Instantiates a new RemoteBlobGet(String prefix, Boolean include).
     *
     * @param prefix {@link String} prefix
     * @param include {@link Boolean} include
     */
    public RemoteBlobGet(String prefix, Boolean include) {
        super(prefix, include);
        this.create = false;
    }
}
