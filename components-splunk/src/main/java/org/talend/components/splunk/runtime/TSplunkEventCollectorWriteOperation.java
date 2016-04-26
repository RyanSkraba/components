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

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;


/**
 * created by dmytro.chmyga on Apr 25, 2016
 */
public class TSplunkEventCollectorWriteOperation implements WriteOperation<WriterResult> {

    private static final long serialVersionUID = 939083892871460237L;

    private final TSplunkEventCollectorSink sink;
    
    public TSplunkEventCollectorWriteOperation(TSplunkEventCollectorSink sink) {
        this.sink = sink;
    }
    
    @Override
    public void initialize(RuntimeContainer adaptor) {
    }

    @Override
    public void finalize(Iterable<WriterResult> writerResults, RuntimeContainer adaptor) {
        //Nothing to be done here.
    }

    @Override
    public Writer<WriterResult> createWriter(RuntimeContainer adaptor) {
        return new TSplunkEventCollectorWriter(this, sink.getServerUrl(), sink.getToken(), sink.getEventsBatchSize(), adaptor);
    }

    @Override
    public Sink getSink() {
        return sink;
    }

}
