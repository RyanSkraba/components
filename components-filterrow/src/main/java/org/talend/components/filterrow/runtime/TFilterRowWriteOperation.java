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
package org.talend.components.filterrow.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;

/**
 * created by dmytro.chmyga on Dec 19, 2016
 */
public class TFilterRowWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = 8273263325653290191L;

    private final Sink sink;

    public TFilterRowWriteOperation(Sink sink) {
        this.sink = sink;
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer arg0) {
        return new TFilterRowWriter(this);
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> arg0, RuntimeContainer arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Sink getSink() {
        return sink;
    }

    @Override
    public void initialize(RuntimeContainer arg0) {
        // TODO Auto-generated method stub

    }

}
