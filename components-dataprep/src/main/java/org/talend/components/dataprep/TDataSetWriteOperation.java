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
package org.talend.components.dataprep;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;

public class TDataSetWriteOperation implements WriteOperation<WriterResult> {

    private Sink sink;
    private RuntimeProperties runtimeProperties;

    public TDataSetWriteOperation(Sink sink, RuntimeProperties runtimeProperties ) {
        this.sink = sink;
        this.runtimeProperties = runtimeProperties;
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer) {
        // Nothing to do here
    }

    @Override
    public void finalize(Iterable<WriterResult> iterable, RuntimeContainer runtimeContainer) {
        // Nothing to do here
    }

    @Override
    public Writer<WriterResult> createWriter(RuntimeContainer runtimeContainer) {
        return new TDataSetOutputWriter(this, runtimeProperties);
    }

    @Override
    public Sink getSink() {
        return sink;
    }
}
