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
package org.talend.components.splunk.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;

public class TSplunkEventCollectorWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = 939083892871460237L;

    private final TSplunkEventCollectorSink sink;

    public TSplunkEventCollectorWriteOperation(TSplunkEventCollectorSink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        System.out.println("weriterREsults: " + writerResults);
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        return new TSplunkEventCollectorWriter(this, sink.getServerUrl(), sink.getToken(), sink.getEventsBatchSize(), adaptor);
    }

    @Override
    public Sink getSink() {
        return sink;
    }

}
