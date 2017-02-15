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

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;

public class AzureStorageQueueWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = -4690204363512647592L;

    private Sink sink;

    protected RuntimeContainer runtime;

    public AzureStorageQueueWriteOperation(Sink sink) {
        this.sink = sink;
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        return new AzureStorageQueueWriter(adaptor, this);
    }

    @Override
    public Sink getSink() {
        return this.sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        runtime = adaptor;
    }
}
