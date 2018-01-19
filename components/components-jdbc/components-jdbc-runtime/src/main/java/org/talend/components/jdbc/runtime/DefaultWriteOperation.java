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
package org.talend.components.jdbc.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;

/**
 * default write operation
 *
 */
abstract public class DefaultWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = 1L;
    
    private Sink sink;

    public DefaultWriteOperation(Sink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer) {
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> iterable, RuntimeContainer runtimeContainer) {
        return Result.accumulateAndReturnMap(iterable);
    }

    @Override
    public Sink getSink() {
        return sink;
    }
}
