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

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.properties.ValidationResult;

/**
 * JDBC SP runtime execution object for output action
 *
 */
public class JDBCSPSink extends JDBCSPSourceOrSink implements Sink {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new JDBCSPWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        return JdbcRuntimeUtils.validate(runtime, this);
    }

}
