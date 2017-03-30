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
package org.talend.components.marketo.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

public class MarketoWriteOperation implements WriteOperation<Result> {

    MarketoSink sink;

    public MarketoWriteOperation(Sink sink) {
        this.sink = (MarketoSink) sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        //
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        if (sink.properties instanceof TMarketoListOperationProperties) {
            return new MarketoListOperationWriter(this, adaptor);
        }
        if (sink.properties instanceof TMarketoOutputProperties) {
            return new MarketoOutputWriter(this, adaptor);
        }
        if (sink.properties instanceof TMarketoInputProperties) {
            return new MarketoInputWriter(this, adaptor);
        }
        return null;
    }

    @Override
    public Sink getSink() {
        return sink;
    }
}
