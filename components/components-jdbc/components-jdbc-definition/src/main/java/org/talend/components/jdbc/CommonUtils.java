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
package org.talend.components.jdbc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferenceProperties.ReferenceType;
import org.talend.components.common.config.jdbc.Dbms;
import org.talend.components.common.config.jdbc.MappingFileLoader;
import org.talend.components.jdbc.module.DBTypes;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.query.EDatabase4DriverClassName;
import org.talend.components.jdbc.query.EDatabaseTypeName;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class CommonUtils {

    /**
     * install the form for the properties
     * 
     * @param props : the properties which the form install on
     * @param formName : the name for the form
     * @return
     */
    public static Form addForm(Properties props, String formName) {
        return new Form(props, formName);
    }

    /**
     * get main schema from the out connector of input components
     * 
     * @param properties
     * @return
     */
    public static Schema getMainSchemaFromOutputConnector(ComponentProperties properties) {
        return getOutputSchema(properties);
    }

    /**
     * get main schema from the input connector of output components
     * 
     * @param properties
     * @return
     */
    public static Schema getMainSchemaFromInputConnector(ComponentProperties properties) {
        Set<? extends Connector> inputConnectors = properties.getPossibleConnectors(false);

        if (inputConnectors == null) {
            return null;
        }

        for (Connector connector : inputConnectors) {
            if (Connector.MAIN_NAME.equals(connector.getName())) {
                return properties.getSchema(connector, false);
            }
        }

        return null;
    }

    /**
     * get the output schema from the properties
     * 
     * @param properties
     * @return
     */
    public static Schema getOutputSchema(ComponentProperties properties) {
        Set<? extends Connector> outputConnectors = properties.getPossibleConnectors(true);

        if (outputConnectors == null) {
            return null;
        }

        for (Connector connector : outputConnectors) {
            if (Connector.MAIN_NAME.equals(connector.getName())) {
                return properties.getSchema(connector, true);
            }
        }

        return null;
    }

    /**
     * get the reject schema from the properties
     * 
     * @param properties
     * @return
     */
    public static Schema getRejectSchema(ComponentProperties properties) {
        Set<? extends Connector> outputConnectors = properties.getPossibleConnectors(true);

        if (outputConnectors == null) {
            return null;
        }

        for (Connector connector : outputConnectors) {
            if (Connector.REJECT_NAME.equals(connector.getName())) {
                return properties.getSchema(connector, true);
            }
        }

        return null;
    }

    /**
     * clone the source schema with a new name and add some fields
     * 
     * @param metadataSchema
     * @param newSchemaName
     * @param moreFields
     * @return
     */
    public static Schema newSchema(Schema metadataSchema, String newSchemaName, List<Schema.Field> moreFields) {
        return newSchema(metadataSchema, newSchemaName, moreFields, metadataSchema.getFields().size());
    }

    public static Schema newSchema(Schema metadataSchema, String newSchemaName, List<Schema.Field> moreFields, int insertPoint) {
        Schema newSchema = Schema.createRecord(newSchemaName, metadataSchema.getDoc(), metadataSchema.getNamespace(),
                metadataSchema.isError());

        List<Field> fields = metadataSchema.getFields();
        List<Schema.Field> copyFieldList = cloneFieldsAndResetPosition(fields);

        copyFieldList.addAll(insertPoint, moreFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String, Object> entry : metadataSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

    private static List<Schema.Field> cloneFieldsAndResetPosition(List<Field> fields) {
        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : fields) {
            Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
            field.getObjectProps().putAll(se.getObjectProps());
            for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }
        return copyFieldList;
    }

    public static Schema mergeRuntimeSchema2DesignSchema4Dynamic(Schema designSchema, Schema runtimeSchema) {
        int dynPosition = Integer.valueOf(designSchema.getProp(ComponentConstants.TALEND6_DYNAMIC_COLUMN_POSITION));
        List<Field> designFields = designSchema.getFields();
        List<Field> runtimeFields = runtimeSchema.getFields();

        int dynamcStartColumnPostion = dynPosition;
        int dynamcEndColumnPostion = dynPosition + runtimeFields.size() - designFields.size();

        Set<String> designFieldSet = new HashSet<>();
        for (Field designField : designFields) {
            String oname = designField.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            designFieldSet.add(oname);
        }

        List<Schema.Field> dynamicFields = new ArrayList<>();

        for (int i = dynamcStartColumnPostion; i < dynamcEndColumnPostion; i++) {
            Field runtimeField = runtimeFields.get(i);
            String oname = runtimeField.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            if (!designFieldSet.contains(oname)) {
                dynamicFields.add(runtimeField);
            }
        }

        dynamicFields = cloneFieldsAndResetPosition(dynamicFields);

        return CommonUtils.newSchema(designSchema, designSchema.getName(), dynamicFields, dynPosition);
    }

    /**
     * fill the connection runtime setting by the connection properties
     * 
     * @param setting
     * @param connection
     */
    public static void setCommonConnectionInfo(AllSetting setting, JDBCConnectionModule connection) {
        if (connection == null) {
            return;
        }

        setting.setDriverPaths(connection.driverTable.drivers.getValue());
        setting.setDriverClass(connection.driverClass.getValue());
        setting.setJdbcUrl(connection.jdbcUrl.getValue());
        setting.setUsername(connection.userPassword.userId.getValue());
        setting.setPassword(connection.userPassword.password.getValue());
    }

    public static boolean setReferenceInfo(AllSetting setting,
            ComponentReferenceProperties<TJDBCConnectionProperties> referencedComponent) {
        if (referencedComponent == null) {
            return false;
        }

        boolean useOtherConnection = useExistedConnection(referencedComponent);

        if (useOtherConnection) {
            setting.setReferencedComponentId(referencedComponent.componentInstanceId.getValue());
            setting.setReferencedComponentProperties(referencedComponent.getReference());
            return true;
        } else {
            return false;
        }
    }

    public static boolean useExistedConnection(ComponentReferenceProperties<TJDBCConnectionProperties> referencedComponent) {
        String refComponentIdValue = referencedComponent.componentInstanceId.getStringValue();

        ReferenceType referenceType = referencedComponent.referenceType.getValue();

        boolean useOtherConnection = refComponentIdValue != null
                && (refComponentIdValue.startsWith(TJDBCConnectionDefinition.COMPONENT_NAME)
                        || (referenceType != null && (referenceType == ReferenceType.COMPONENT_INSTANCE)));
        return useOtherConnection;
    }

    public static void setReferenceInfoAndConnectionInfo(AllSetting setting,
            ComponentReferenceProperties<TJDBCConnectionProperties> referencedComponent, JDBCConnectionModule connection) {
        boolean useExistedConnection = setReferenceInfo(setting, referencedComponent);

        if (useExistedConnection) {
            if (referencedComponent.getReference() != null) {// avoid the NPE in JdbcRuntimeInfo
                setCommonConnectionInfo(setting, ((TJDBCConnectionProperties) referencedComponent.getReference()).connection);
            }
        } else {
            setCommonConnectionInfo(setting, connection);
        }
    }

    public static List<String> getAllSchemaFieldNames(Schema schema) {
        List<String> values = new ArrayList<>();

        if (schema == null) {
            return values;
        }

        for (Schema.Field field : schema.getFields()) {
            values.add(field.name());
        }

        return values;
    }

    private static Pattern pattern = Pattern.compile(
            "^SELECT\\s+((?!((\\bINTO\\b)|(\\bFOR\\s+UPDATE\\b)|(\\bLOCK\\s+IN\\s+SHARE\\s+MODE\\b))).)+$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * validate if the sql is a pure query statement which don't write or lock something, if a query, return it, if not, throw
     * some exception
     * 
     * @param query
     * @return
     */
    public static String validateQuery(String query) {
        if ((query == null) || "".equals(query) || pattern.matcher(query.trim()).matches()) {
            return query;
        }

        throw TalendRuntimeException.createUnexpectedException(
                "Please check your sql as we only allow the query which don't do write or lock action.");
    }

    public static List<String> getAllSchemaFieldDBNames(Schema schema) {
        List<String> values = new ArrayList<>();

        if (schema == null) {
            return values;
        }

        for (Schema.Field field : schema.getFields()) {
            values.add(field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        }

        return values;
    }

    // the code below come from azure, will move to tcomp common
    public static String getStudioNameFromProperty(final String inputString) {
        StringBuilder result = new StringBuilder();
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }

        for (int i = 0; i < inputString.length(); i++) {
            Character c = inputString.charAt(i);
            result.append(Character.isUpperCase(c) && i > 0 ? "_" + c : c);
        }
        return result.toString().toUpperCase(Locale.ENGLISH);
    }

    public static Schema.Field getField(Schema schema, String fieldName) {
        if (schema == null) {
            return null;
        }

        for (Schema.Field outField : schema.getFields()) {
            if (outField.name().equals(fieldName)) {
                return outField;
            }
        }

        return null;
    }

    /**
     * computer the real database type by driver jar and class, this is useful for the tjdbcxxx
     * 
     * @param setting
     * @param dbType
     * @return
     */
    public static String getRealDBType(AllSetting setting, String dbType) {
        if (dbType == null || dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName())) {
            String driverClassName = setting.getDriverClass();

            if ("com.sybase.jdbc3.jdbc.SybDataSource".equals(driverClassName)) {
                driverClassName = EDatabase4DriverClassName.SYBASEASE.getDriverClass();
            }

            List<String> driverPaths = setting.getDriverPaths();
            StringBuilder sb = new StringBuilder();
            for (String path : driverPaths) {
                sb.append(path);
            }
            String driverJarInfo = sb.toString();

            dbType = getDbTypeByClassNameAndDriverJar(driverClassName, driverJarInfo);

            if (dbType == null) {
                // if we can not get the DB Type from the existing driver list, just set back the type to ORACLE
                // since it's one DB unknown from Talend.
                // it might not work directly for all DB, but it will generate a standard query.
                dbType = EDatabaseTypeName.ORACLE_OCI.getDisplayName();
            }
        }
        return dbType;
    }

    public static Dbms getMapping(URL mappingFilesDir, AllSetting setting,
            String dbTypeByComponentType/*
                                         * for example, if tjdbcxxx, the type is "General JDBC", if tmysqlxxx, the type is "MySQL"
                                         */,
            DBTypes dbTypeInComponentSetting/* in tjdbcinput, can choose the db type in advanced setting */) {
        final String realDbType = getRealDBType(setting, dbTypeByComponentType);
        final String product = EDatabaseTypeName.getTypeFromDisplayName(realDbType).getProduct();

        String mappingFileSubfix = productValue2DefaultMappingFileSubfix.get(product);

        if ((dbTypeInComponentSetting != null) && (mappingFileSubfix == null)) {
            mappingFileSubfix = dbType2MappingFileSubfix.get(dbTypeInComponentSetting);
        }

        if (mappingFileSubfix == null) {
            mappingFileSubfix = "Mysql";
        }

        File mappingFileFullPath = new File(mappingFilesDir.getFile(), "mapping_" + mappingFileSubfix + ".xml");
        if (!mappingFileFullPath.exists()) {
            mappingFileFullPath = new File(mappingFilesDir.getFile(), "mapping_" + mappingFileSubfix.toLowerCase() + ".xml");
        }

        MappingFileLoader fileLoader = new MappingFileLoader();
        List<Dbms> dbmsList = fileLoader.load(mappingFileFullPath);
        Dbms dbms = dbmsList.get(0);

        return dbms;
    }

    public static Dbms getMapping(String mappingFilesDir, AllSetting setting,
            String dbTypeByComponentType/*
                                         * for example, if tjdbcxxx, the type is "General JDBC", if tmysqlxxx, the type is "MySQL"
                                         */,
            DBTypes dbTypeInComponentSetting/* in tjdbcinput, can choose the db type in advanced setting */) {
        try {
            return getMapping(new URL(mappingFilesDir), setting, dbTypeByComponentType, dbTypeInComponentSetting);
        } catch (MalformedURLException e) {
            throw new RuntimeException("can't find the mapping file dir : " + mappingFilesDir);
        }
    }

    // now we use a inside mapping to do the mapping file search, not good and easy to break, TODO should load all the mapping
    // files to memory only once, and search by the memory object
    private static Map<String, String> productValue2DefaultMappingFileSubfix = new HashMap<>();

    private static Map<DBTypes, String> dbType2MappingFileSubfix = new HashMap<>();

    static {
        dbType2MappingFileSubfix.put(DBTypes.AS400, "AS400");
        dbType2MappingFileSubfix.put(DBTypes.ACCESS, "Access");
        dbType2MappingFileSubfix.put(DBTypes.DB2, "IBMDB2");
        dbType2MappingFileSubfix.put(DBTypes.FIREBIRD, "Firebird");
        dbType2MappingFileSubfix.put(DBTypes.HSQLDB, "HSQLDB");
        dbType2MappingFileSubfix.put(DBTypes.INFORMIX, "Informix");
        dbType2MappingFileSubfix.put(DBTypes.INGRES, "Ingres");
        dbType2MappingFileSubfix.put(DBTypes.VECTORWISE, "VectorWise");
        dbType2MappingFileSubfix.put(DBTypes.INTERBASE, "Interbase");
        dbType2MappingFileSubfix.put(DBTypes.JAVADB, "JavaDB");
        dbType2MappingFileSubfix.put(DBTypes.MAXDB, "MaxDB");
        dbType2MappingFileSubfix.put(DBTypes.MSSQL, "MSSQL");
        dbType2MappingFileSubfix.put(DBTypes.MYSQL, "Mysql");
        dbType2MappingFileSubfix.put(DBTypes.NETEZZA, "Netezza");
        dbType2MappingFileSubfix.put(DBTypes.ORACLE, "Oracle");
        dbType2MappingFileSubfix.put(DBTypes.POSTGRESQL, "Postgres");
        dbType2MappingFileSubfix.put(DBTypes.POSTGREPLUS, "PostgresPlus");
        dbType2MappingFileSubfix.put(DBTypes.SQLITE, "SQLite");
        dbType2MappingFileSubfix.put(DBTypes.SYBASE, "Sybase");
        dbType2MappingFileSubfix.put(DBTypes.SAPHANA, "SAPHana");
        dbType2MappingFileSubfix.put(DBTypes.TERADATA, "Teradata");
        dbType2MappingFileSubfix.put(DBTypes.VERTICA, "Vertica");
        dbType2MappingFileSubfix.put(DBTypes.H2, "H2");
        dbType2MappingFileSubfix.put(DBTypes.ODBC, "MSODBC");
    }

    static {
        productValue2DefaultMappingFileSubfix.put("ACCESS", "Access");
        productValue2DefaultMappingFileSubfix.put("AS400", "AS400");
        productValue2DefaultMappingFileSubfix.put("BIGQUERY", "BigQuery");
        productValue2DefaultMappingFileSubfix.put("Cassandra", "Cassandra");
        // productValue2DefaultMappingFileSubfix.put("Cassandra", "Cassandra_datastax");
        // productValue2DefaultMappingFileSubfix.put("Cassandra", "Cassandra22_datastax");
        productValue2DefaultMappingFileSubfix.put("Exasol", "Exasol");
        productValue2DefaultMappingFileSubfix.put("FIREBIRD", "Firebird");
        productValue2DefaultMappingFileSubfix.put("GREENPLUM", "Greenplum");
        productValue2DefaultMappingFileSubfix.put("H2", "H2");
        productValue2DefaultMappingFileSubfix.put("HIVE", "Hive");
        productValue2DefaultMappingFileSubfix.put("HSQLDB", "HSQLDB");
        productValue2DefaultMappingFileSubfix.put("IBM_DB2", "IBMDB2");
        productValue2DefaultMappingFileSubfix.put("IMPALA", "Impala");
        productValue2DefaultMappingFileSubfix.put("INFORMIX", "Informix");
        productValue2DefaultMappingFileSubfix.put("INGRES", "Ingres");
        productValue2DefaultMappingFileSubfix.put("INTERBASE", "Interbase");
        productValue2DefaultMappingFileSubfix.put("JAVADB", "JavaDB");
        productValue2DefaultMappingFileSubfix.put("MAXDB", "MaxDB");
        productValue2DefaultMappingFileSubfix.put("ODBC", "MsOdbc");
        productValue2DefaultMappingFileSubfix.put("SQL_SERVER", "MSSQL");
        productValue2DefaultMappingFileSubfix.put("MYSQL", "Mysql");
        productValue2DefaultMappingFileSubfix.put("NETEZZA", "Netezza");
        productValue2DefaultMappingFileSubfix.put("ORACLE", "Oracle");
        productValue2DefaultMappingFileSubfix.put("PARACCEL", "ParAccel");
        productValue2DefaultMappingFileSubfix.put("POSTGRESQL", "Postgres");
        productValue2DefaultMappingFileSubfix.put("POSTGRESPLUS", "PostgresPlus");
        productValue2DefaultMappingFileSubfix.put("REDSHIFT", "Redshift");
        productValue2DefaultMappingFileSubfix.put("SAPHANA", "SAPHana");
        productValue2DefaultMappingFileSubfix.put("SNOWFLAKE", "Snowflake");
        productValue2DefaultMappingFileSubfix.put("SQLITE", "SQLite");
        productValue2DefaultMappingFileSubfix.put("SYBASE", "Sybase");
        productValue2DefaultMappingFileSubfix.put("TERADATA", "Teradata");
        productValue2DefaultMappingFileSubfix.put("VECTORWISE", "VectorWise");
        productValue2DefaultMappingFileSubfix.put("VERTICA", "Vertica");
    }

    // hywang add for bug 7575
    private static String getDbTypeByClassNameAndDriverJar(String driverClassName, String driverJar) {
        List<EDatabase4DriverClassName> t4d = EDatabase4DriverClassName.indexOfByDriverClass(driverClassName);
        if (t4d.size() == 1) {
            return t4d.get(0).getDbTypeName();
        } else if (t4d.size() > 1) {
            // for some dbs use the same driverClassName.
            if (driverJar == null || "".equals(driverJar) || !driverJar.contains(".jar")) {
                return t4d.get(0).getDbTypeName();
            } else if (driverJar.contains("postgresql-8.3-603.jdbc3.jar") || driverJar.contains("postgresql-8.3-603.jdbc4.jar")
                    || driverJar.contains("postgresql-8.3-603.jdbc2.jar")) {
                return EDatabase4DriverClassName.PSQL.getDbTypeName();
            } else {
                return t4d.get(0).getDbTypeName(); // first default
            }
        }
        return null;
    }

    public static void updatePossibleValues(Property<String> property, List<String> possibleValues) {
        property.setPossibleValues(possibleValues);
        String oldValue = property.getValue();
        if (possibleValues != null && !possibleValues.isEmpty() && !possibleValues.contains(oldValue)) {
            property.setStoredValue(possibleValues.get(0));
        } else if ((possibleValues == null) || possibleValues.isEmpty()) {
            property.setStoredValue(null);
        }
    }

}
