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
package org.talend.components.jira.testutils;

import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Jira docker image server constants, which are used in tests.
 * One single place to change this properties
 */
public final class JiraTestConstants {

    /**
     * Jira server host and port
     */
    public static final String HOST_PORT;
    
    /**
     * Incorrect host and port
     */
    public static final String INCORRECT_HOST_PORT = "http://incorrecthost.com";

    /**
     * Jira server user id
     */
    public static final String USER = "root";
    
    /**
     * Jira server wrong user id
     */
    public static final String WRONG_USER = "wrongUser";

    /**
     * Empty user constant
     */
    public static final String ANONYMOUS_USER = "";

    /**
     * Jira server user id
     */
    public static final String PASS = "123456";
    
    /**
     * Schemas
     */
    public static final Schema DELETE_SCHEMA;

    public static final Schema INSERT_SCHEMA;

    public static final Schema UPDATE_SCHEMA;
    
    /**
     * Sets default HOST_PORT in case of running tests from IDE
     */
    static {
        String systemPropertyHost = System.getProperty("jira.host");
        HOST_PORT = systemPropertyHost != null ?  systemPropertyHost : "http://192.168.99.100:8080/";
        
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();

        Schema.Field deleteIdField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        DELETE_SCHEMA = Schema.createRecord("jira", null, null, false, Collections.singletonList(deleteIdField));
        DELETE_SCHEMA.addProp(TALEND_IS_LOCKED, "true");

        Schema.Field insertJsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        INSERT_SCHEMA = Schema.createRecord("jira", null, null, false, Collections.singletonList(insertJsonField));
        INSERT_SCHEMA.addProp(TALEND_IS_LOCKED, "true");

        Schema.Field updateIdField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        Schema.Field updateJsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        List<Schema.Field> fields = Arrays.asList(updateIdField, updateJsonField);
        UPDATE_SCHEMA = Schema.createRecord("jira", null, null, false, fields);
        UPDATE_SCHEMA.addProp(TALEND_IS_LOCKED, "true");
    }
}
