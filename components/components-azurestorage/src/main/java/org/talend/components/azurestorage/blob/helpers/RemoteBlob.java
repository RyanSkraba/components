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
 * Class RemoteBlob.
 *
 * Used by the {@link RemoteBlobsTable} for filtering remote blobs.
 */
public class RemoteBlob {

    /** prefix - remote blob prefix. */
    public String prefix;

    /** include - include sub-directories. */
    public Boolean include = false;

    /**
     * Instantiates a new RemoteBlob(String prefix, Boolean include).
     *
     * @param prefix {@link String} prefix
     * @param include {@link Boolean} include
     */
    public RemoteBlob(String prefix, Boolean include) {
        this.prefix = prefix;
        this.include = include;
    }
}
