// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.components.common.avro.JDBCResultSetIndexedRecordConverter;

public class SnowflakeResultSetIndexedRecordConverter extends JDBCResultSetIndexedRecordConverter {

    @Override
    protected JDBCAvroRegistry getRegistry() {
        return SnowflakeAvroRegistry.get();
    }

    @Override
    protected void resetSizeByResultSet(ResultSet resultSet) {
        try {
            this.setSizeInResultSet(resultSet.getMetaData().getColumnCount());
        } catch (SQLException e) {
            throw new ComponentException(e);
        }
    }

}
