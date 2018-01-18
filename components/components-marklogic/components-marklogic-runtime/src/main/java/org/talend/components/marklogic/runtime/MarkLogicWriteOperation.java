// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.runtime;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputProperties;

import java.util.Map;

public class MarkLogicWriteOperation implements  WriteOperation<Result> {
    private MarkLogicSink markLogicSink;
    private MarkLogicOutputProperties outputProperties;
    @Override
    public void initialize(RuntimeContainer adaptor) {
        //nothing to do here
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public MarkLogicWriter createWriter(RuntimeContainer adaptor) {
        return new MarkLogicWriter(this, adaptor, outputProperties);
    }

    @Override
    public MarkLogicSink getSink() {
        return markLogicSink;
    }

    public MarkLogicWriteOperation(MarkLogicSink markLogicSink, MarkLogicOutputProperties outputProperties) {
        this.markLogicSink = markLogicSink;
        this.outputProperties = outputProperties;
    }
}
