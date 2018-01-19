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

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.runtime.writer.JDBCSPWriter;

/**
 * JDBC SP write operation
 *
 */
public class JDBCSPWriteOperation extends DefaultWriteOperation {

    private static final long serialVersionUID = 1L;

    public JDBCSPWriteOperation(Sink sink) {
        super(sink);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer runtimeContainer) {
        return new JDBCSPWriter(this, runtimeContainer);
    }

}
