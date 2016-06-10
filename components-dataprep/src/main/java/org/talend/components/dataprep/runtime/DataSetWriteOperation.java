// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep.runtime;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;

import java.util.Map;

public class DataSetWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = 7572409371695331857L;

    private Sink sink;

    public DataSetWriteOperation(Sink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer) {
        // Nothing to do here
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> iterable, RuntimeContainer runtimeContainer) {
        return Result.accumulateAndReturnMap(iterable);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer runtimeContainer) {
        return new DataSetWriter(this);
    }

    @Override
    public Sink getSink() {
        return sink;
    }
}
