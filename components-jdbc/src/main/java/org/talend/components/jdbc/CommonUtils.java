// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;

public class CommonUtils {

    public static Schema getSchema(SchemaProperties schema) {
        return schema.schema.getValue();
    }

    public static Form addForm(Properties props, String formName) {
        return new Form(props, formName);
    }

    // get main schema from the out connector of input components
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

    // get main schema from the input connector of output components
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

    public static void setCommonConnectionInfo(AllSetting setting, JDBCConnectionModule connection) {
        setting.setDriverPaths(connection.driverTable.drivers.getValue());
        setting.setDriverClass(connection.driverClass.getValue());
        setting.setJdbcUrl(connection.jdbcUrl.getValue());
        setting.setUsername(connection.userPassword.userId.getValue());
        setting.setPassword(connection.userPassword.password.getValue());
    }
}
