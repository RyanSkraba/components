// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.Map;

import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;

final class SalesforceWriteOperation implements WriteOperation<Map<String, Object>> {

    private SalesforceSink ssink;

    public SalesforceWriteOperation(SalesforceSink ssink) {
        this.ssink = ssink;
    }

    @Override
    public void initialize(Adaptor adaptor) {
        // TODO Auto-generated method stub

    }

    @Override
    public Sink getSink() {
        return ssink;
    }

    @Override
    public void finalize(Iterable<Map<String, Object>> writerResults, Adaptor adaptor) {
        // TODO Auto-generated method stub

    }

    @Override
    public Writer<Map<String, Object>> createWriter(Adaptor adaptor) {
        return new Writer<Map<String, Object>>() {

            @Override
            public void open(String uId) throws IOException {
                // TODO Auto-generated method stub

            }

            @Override
            public void write(Object object) throws IOException {
                // TODO Auto-generated method stub

            }

            @Override
            public Map<String, Object> close() throws IOException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public WriteOperation<Map<String, Object>> getWriteOperation() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
}