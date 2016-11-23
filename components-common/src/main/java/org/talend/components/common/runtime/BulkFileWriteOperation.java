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
package org.talend.components.common.runtime;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;

import java.util.Map;

public class BulkFileWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = 1L;

    private BulkFileSink fileSink;

    public BulkFileWriteOperation(BulkFileSink fileSink) {
        this.fileSink = fileSink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
    }

    public Sink getSink() {
        return fileSink;
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        return new BulkFileWriter(this, fileSink.getBulkFileProperties(), adaptor);
    }

}