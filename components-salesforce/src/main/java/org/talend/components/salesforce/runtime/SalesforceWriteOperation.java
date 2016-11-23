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
package org.talend.components.salesforce.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;

public final class SalesforceWriteOperation implements WriteOperation<Result> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private SalesforceSink ssink;

    public SalesforceWriteOperation(SalesforceSink ssink) {
        this.ssink = ssink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {
        // Nothing to be done.
    }

    @Override
    public SalesforceSink getSink() {
        return ssink;
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public SalesforceWriter createWriter(RuntimeContainer adaptor) {
        return new SalesforceWriter(this, adaptor);
    }

}
