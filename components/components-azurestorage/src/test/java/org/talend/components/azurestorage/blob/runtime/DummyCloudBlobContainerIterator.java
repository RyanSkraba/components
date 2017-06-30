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

import java.util.Iterator;
import java.util.List;

import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class DummyCloudBlobContainerIterator implements Iterator<CloudBlobContainer> {

    private Iterator<CloudBlobContainer> it;

    public DummyCloudBlobContainerIterator(List<CloudBlobContainer> list) {
        super();
        this.it = list.iterator();
    }

    @Override
    public boolean hasNext() {

        return it.hasNext();
    }

    @Override
    public CloudBlobContainer next() {
        return it.next();
    }

}
