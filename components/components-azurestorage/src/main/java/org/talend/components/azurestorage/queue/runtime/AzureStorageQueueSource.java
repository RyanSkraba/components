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

import java.util.List;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;

public class AzureStorageQueueSource extends AzureStorageQueueSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = 738753429557646099L;

    @SuppressWarnings("rawtypes")
    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        
        if (properties instanceof TAzureStorageQueueListProperties) {
            return new AzureStorageQueueListReader(container, this, (TAzureStorageQueueListProperties) properties);
        }
        if (properties instanceof TAzureStorageQueueInputLoopProperties) {
            return new AzureStorageQueueInputLoopReader(container, this, (TAzureStorageQueueInputLoopProperties) properties);
        }
        if (properties instanceof TAzureStorageQueueInputProperties) {
            return new AzureStorageQueueInputReader(container, this, (TAzureStorageQueueInputProperties) properties);
        }
        return null;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        return null;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

}
