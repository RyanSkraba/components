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
package org.talend.components.azurestorage.queue.runtime;

import java.util.Iterator;
import java.util.List;

import com.microsoft.azure.storage.queue.CloudQueue;

public class DummyCloudQueueIterator implements Iterator<CloudQueue> {

    private Iterator<CloudQueue> it;

    public DummyCloudQueueIterator(List<CloudQueue> list) {
        super();
        this.it = list.iterator();
    }

    @Override
    public boolean hasNext() {

        return it.hasNext();
    }

    @Override
    public CloudQueue next() {
        return it.next();
    }
}
