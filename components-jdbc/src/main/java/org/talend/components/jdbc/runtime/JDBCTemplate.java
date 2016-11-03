package org.talend.components.jdbc.runtime;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.PreparedStatementTable;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.runtime.RuntimeInfo;

public class JDBCTemplate {

    public static Connection createConnection(AllSetting setting) throws ClassNotFoundException, SQLException {
        java.lang.Class.forName(setting.getDriverClass());
        Connection conn = java.sql.DriverManager.getConnection(setting.getJdbcUrl(), setting.getUsername(),
                setting.getPassword());
        return conn;
    }

    public static List<Schema.Field> getKeyColumns(List<Schema.Field> allFields) {
        List<Schema.Field> result = new ArrayList<Schema.Field>();

        for (Schema.Field field : allFields) {
            boolean isKey = "true".equalsIgnoreCase(field.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
            if (isKey) {
                result.add(field);
            }
        }

        return result;
    }

    public static List<Schema.Field> getValueColumns(List<Schema.Field> allFields) {
        List<Schema.Field> result = new ArrayList<Schema.Field>();

        for (Schema.Field field : allFields) {
            boolean isKey = "true".equalsIgnoreCase(field.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));

            if (!isKey) {
                result.add(field);
            }
        }

        return result;
    }

    public static RuntimeInfo createCommonRuntime(final ClassLoader classLoader, final Properties properties,
            final String runtimeClassName) {
        return new RuntimeInfo() {

            @Override
            public String getRuntimeClassName() {
                return runtimeClassName;
            }

            @Override
            public List<URL> getMavenUrlDependencies() {
                List<URL> result = new ArrayList<URL>();

                DependenciesReader dependenciesReader = new DependenciesReader("org.talend.components", "components-jdbc");
                Set<String> default_dependencies = null;
                try {
                    default_dependencies = dependenciesReader.getDependencies(classLoader);
                } catch (IOException e) {
                    throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, e,
                            ExceptionContext.withBuilder().put("path", dependenciesReader.getDependencyFilePath()).build());
                }

                try {
                    if (default_dependencies != null) {
                        for (String dependency : default_dependencies) {
                            result.add(new URL(dependency));
                        }
                    }

                    if (properties != null) {
                        final RuntimeSettingProvider props = (RuntimeSettingProvider) properties;
                        List<String> drivers = props.getRuntimeSetting().getDriverPaths();
                        if (drivers != null) {
                            for (String driver : drivers) {
                                result.add(new URL(removeQuote(driver)));
                            }
                        }
                    }

                } catch (MalformedURLException e) {
                    throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                }

                return result;
            }
        };
    }

    private static String removeQuote(String content) {
        if (content.startsWith("\"") && content.endsWith("\"")) {
            return content.substring(1, content.length() - 1);
        }

        return content;
    }

    public static void setPreparedStatement(final PreparedStatement pstmt, final List<Integer> indexs, final List<String> types,
            final List<Object> values) throws SQLException {
        // TODO : adjust it
        for (int i = 0; i < indexs.size(); i++) {
            Integer index = indexs.get(i);
            PreparedStatementTable.Type type = PreparedStatementTable.Type.valueOf(types.get(i));
            Object value = values.get(i);

            switch (type) {
            case BigDecimal:
                pstmt.setBigDecimal(index, (BigDecimal) value);
                break;
            case Blob:
                pstmt.setBlob(index, (Blob) value);
                break;
            case Boolean:
                pstmt.setBoolean(index, (boolean) value);
                break;
            case Byte:
                pstmt.setByte(index, (byte) value);
                break;
            case Bytes:
                pstmt.setBytes(index, (byte[]) value);
                break;
            case Clob:
                pstmt.setClob(index, (Clob) value);
                break;
            case Date:
                pstmt.setTimestamp(index, new Timestamp(((Date) value).getTime()));
                break;
            case Double:
                pstmt.setDouble(index, (double) value);
                break;
            case Float:
                pstmt.setFloat(index, (float) value);
                break;
            case Int:
                pstmt.setInt(index, (int) value);
                break;
            case Long:
                pstmt.setLong(index, (long) value);
                break;
            case Object:
                pstmt.setObject(index, value);
                break;
            case Short:
                pstmt.setShort(index, (short) value);
                break;
            case String:
                pstmt.setString(index, (String) value);
                break;
            case Time:
                pstmt.setTime(index, (Time) value);
                break;
            case Null:
                pstmt.setNull(index, (int) value);
                break;
            default:
                pstmt.setString(index, (String) value);
                break;
            }
        }
    }

}
