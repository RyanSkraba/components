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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;

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
        Schema newSchema = Schema.createRecord(newSchemaName, metadataSchema.getDoc(), metadataSchema.getNamespace(),
                metadataSchema.isError());

        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : metadataSchema.getFields()) {
            Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
            field.getObjectProps().putAll(se.getObjectProps());
            for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }

        copyFieldList.addAll(moreFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String, Object> entry : metadataSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

    /**
     * fill the connection runtime setting by the connection properties
     * 
     * @param setting
     * @param connection
     */
    public static void setCommonConnectionInfo(AllSetting setting, JDBCConnectionModule connection) {
        setting.setDriverPaths(connection.driverTable.drivers.getValue());
        setting.setDriverClass(connection.driverClass.getValue());
        setting.setJdbcUrl(connection.jdbcUrl.getValue());
        setting.setUsername(connection.userPassword.userId.getValue());
        setting.setPassword(connection.userPassword.password.getValue());
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

}
