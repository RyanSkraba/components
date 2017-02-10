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

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;

public class AzureStorageQueueSink extends AzureStorageQueueSourceOrSink implements Sink {

    private static final long serialVersionUID = 2888690608375652275L;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new AzureStorageQueueWriteOperation(this);
    }

}
