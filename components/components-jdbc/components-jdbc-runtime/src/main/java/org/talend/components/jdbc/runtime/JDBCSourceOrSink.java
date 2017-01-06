
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.avro.JDBCAvroRegistryString;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.JdbcComponentErrorsCode;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * common JDBC runtime execution object
 *
 */
public class JDBCSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -1730391293657968628L;

    public RuntimeSettingProvider properties;

    protected AllSetting setting;

    private Connection conn;

    @Override
    public ValidationResult initialize(RuntimeContainer runtime, ComponentProperties properties) {
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();
        return ValidationResult.OK;
    }

    // TODO adjust it, now only as a temp workaround for reuse current class in the datastore runtime
    public ValidationResult initialize(RuntimeContainer runtime, DatastoreProperties properties) {
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();
        return ValidationResult.OK;
    }

    // TODO adjust it, now only as a temp workaround for reuse current class in the dataset runtime
    @SuppressWarnings("rawtypes")
    public ValidationResult initialize(RuntimeContainer runtime, DatasetProperties properties) {
        this.properties = (RuntimeSettingProvider) properties;
        setting = this.properties.getRuntimeSetting();
        return ValidationResult.OK;
    }

    private static ValidationResult fillValidationResult(ValidationResult vr, Exception ex) {
        if (vr == null) {
            return null;
        }

        if (ex.getMessage() == null || ex.getMessage().isEmpty()) {
            vr.setMessage(ex.toString());
        } else {
            vr.setMessage(ex.getMessage());
        }
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResult vr = new ValidationResult();
        // TODO The connection should not be maintained just for validation. But as it is store in the runtime context, could not
        // close it here.
        try {
            conn = connect(runtime);
        } catch (Exception ex) {
            fillValidationResult(vr, ex);
        }
        return vr;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtime) throws IOException {
        List<NamedThing> result = new ArrayList<>();
        try (Connection conn = connect(runtime);
                ResultSet resultset = conn.getMetaData().getTables(null, null, null, new String[] { "TABLE" })) {
            while (resultset.next()) {
                String tablename = resultset.getString("TABLE_NAME");
                result.add(new SimpleNamedThing(tablename, tablename));
            }
        } catch (Exception e) {
            throw new ComponentException(fillValidationResult(new ValidationResult(), e));
        }
        return result;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtime, String tableName) throws IOException {
        try (Connection conn = connect(runtime);
                ResultSet resultset = conn.getMetaData().getColumns(null, null, tableName, null)) {
            return JDBCAvroRegistryString.get().inferSchema(resultset);
        } catch (Exception e) {
            throw new ComponentException(e);
        }
    }

    public Schema getSchemaFromQuery(RuntimeContainer runtime, String query) {
        try (Connection conn = connect(runtime);
                Statement statement = conn.createStatement();
                ResultSet resultset = statement.executeQuery(query)) {
            ResultSetMetaData metadata = resultset.getMetaData();
            return JDBCAvroRegistryString.get().inferSchema(metadata);
        } catch (SQLSyntaxErrorException sqlSyntaxException) {
            throw new ComponentException(JdbcComponentErrorsCode.SQL_SYNTAX_ERROR, sqlSyntaxException);
        } catch (SQLException e) {
            throw new ComponentException(JdbcComponentErrorsCode.SQL_ERROR, e);
        } catch (ClassNotFoundException e) {
            throw new ComponentException(JdbcComponentErrorsCode.DRIVER_NOT_PRESENT_ERROR, e);
        }
    }

    public Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        // TODO now we use routines.system.TalendDataSource to get the data connection from the ESB runtime, but now we
        // can't
        // refer it by the new framework, so will fix it later

        // TODO routines.system.SharedDBConnectionLog4j, the same with the TODO above

        AllSetting setting = properties.getRuntimeSetting();

        // connection component
        Connection conn = JdbcRuntimeUtils.createConnection(setting);

        Boolean autoCommit = setting.getUseAutoCommit();
        if (autoCommit != null && autoCommit) {
            conn.setAutoCommit(setting.getAutocommit());
        }

        if (runtime != null) {
            runtime.setComponentData(runtime.getCurrentComponentId(), ComponentConstants.CONNECTION_KEY, conn);
        }

        return conn;
    }

    public Connection getConnection(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        if (conn == null) {
            conn = connect(runtime);
        }
        return conn;
    }

}
