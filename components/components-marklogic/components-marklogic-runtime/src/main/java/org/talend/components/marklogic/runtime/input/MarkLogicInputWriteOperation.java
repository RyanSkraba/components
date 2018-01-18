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
package org.talend.components.marklogic.runtime.input;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;

import java.util.Map;

public class MarkLogicInputWriteOperation implements WriteOperation<Result> {

    private MarkLogicInputSink inputSink;

    private MarkLogicInputProperties inputProperties;

    public MarkLogicInputWriteOperation(MarkLogicInputSink markLogicInputSink, MarkLogicInputProperties inputProperties) {
        this.inputSink = markLogicInputSink;
        this.inputProperties = inputProperties;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        //nothing to do here
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> readResults, RuntimeContainer adaptor) {
        return ResultWithLongNB.accumulateAndReturnMap(readResults);
    }

    @Override
    public MarkLogicRowProcessor createWriter(RuntimeContainer adaptor) {
        //Row reader is actually writer which takes docId and write docId and docContent result as feedback
        return new MarkLogicRowProcessor(this, adaptor, inputProperties);
    }

    @Override
    public MarkLogicInputSink getSink() {
        return inputSink;
    }
}
