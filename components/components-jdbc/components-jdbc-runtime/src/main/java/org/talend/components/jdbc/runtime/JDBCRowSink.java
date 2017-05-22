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
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

/**
 * JDBC row runtime execution object for output action
 *
 */
public class JDBCRowSink extends JDBCRowSourceOrSink implements Sink {

    private static final long serialVersionUID = 3228265006313531905L;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new JDBCRowWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResultMutable vr = new ValidationResultMutable();
        try {
            connect(runtime);
        } catch (Exception ex) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(ex.getMessage());
        }
        return vr;
    }

}
