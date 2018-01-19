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
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.writer.JDBCOutputDeleteWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputInsertOrUpdateWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputInsertWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputUpdateOrInsertWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputUpdateWriter;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;

/**
 * common JDBC output write operation
 *
 */
public class JDBCOutputWriteOperation extends DefaultWriteOperation {

    private static final long serialVersionUID = 1L;

    public JDBCOutputWriteOperation(Sink sink) {
        super(sink);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer runtimeContainer) {
        RuntimeSettingProvider properties = ((JDBCSink) this.getSink()).properties;

        DataAction dataAction = properties.getRuntimeSetting().getDataAction();

        switch (dataAction) {
        case INSERT:
            return new JDBCOutputInsertWriter(this, runtimeContainer);
        case UPDATE:
            return new JDBCOutputUpdateWriter(this, runtimeContainer);
        case DELETE:
            return new JDBCOutputDeleteWriter(this, runtimeContainer);
        case INSERT_OR_UPDATE:
            return new JDBCOutputInsertOrUpdateWriter(this, runtimeContainer);
        case UPDATE_OR_INSERT:
            return new JDBCOutputUpdateOrInsertWriter(this, runtimeContainer);
        default:
            return null;
        }

    }

}
