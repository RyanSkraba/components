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
package org.talend.components.jdbc.runtime.writer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.jdbc.runtime.JDBCRollbackSink;

/**
 * the JDBC writer for JDBC Commit
 *
 */
public class JDBCRollbackWriter implements Writer<Result> {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCRollbackWriter.class);

    private WriteOperation<Result> writeOperation;

    private JDBCRollbackSink sink;

    private Result result;

    private RuntimeContainer runtime;

    public JDBCRollbackWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        this.writeOperation = writeOperation;
        sink = (JDBCRollbackSink) writeOperation.getSink();
        this.runtime = runtime;

        result = new Result();
    }

    public void open(String uId) throws IOException {
        // do nothing
    }

    public void write(Object datum) throws IOException {
        result.totalCount++;

        try {
            sink.doRollbackAction(runtime);
        } catch (Exception e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Result close() throws IOException {
        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

}
