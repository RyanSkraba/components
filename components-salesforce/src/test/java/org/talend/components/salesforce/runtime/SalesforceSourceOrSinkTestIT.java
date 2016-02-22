// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement.Type;
import org.talend.daikon.schema.SchemaFactory;

import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

public class SalesforceSourceOrSinkTestIT {

    private static final boolean ADD_QUOTES = true;

    String userId;

    String password;

    String securityKey;

    public SalesforceSourceOrSinkTestIT() {
        userId = System.getProperty("salesforce.user");
        password = System.getProperty("salesforce.password");
        securityKey = System.getProperty("salesforce.key");
    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSourceOrSink#initialize(org.talend.components.api.adaptor.Adaptor, org.talend.components.api.properties.ComponentProperties)}
     * .
     */
    @Test
    public void testInitialize() {
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        TSalesforceInputProperties properties = new TSalesforceInputProperties(null);
        salesforceSourceOrSink.initialize(null, properties);
        assertEquals(properties.connection, salesforceSourceOrSink.getConnectionProperties());
    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSourceOrSink#validate(org.talend.components.api.adaptor.Adaptor)}
     * .
     */
    @Test
    public void testValidate() {
        // check validate is OK with proper creadentials
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, props);
        assertEquals(Result.OK, salesforceSourceOrSink.validate(null).getStatus());
        // check validate is ERROR with wrong creadentials
        props.userPassword.userId.setValue("");
        assertEquals(Result.ERROR, salesforceSourceOrSink.validate(null).getStatus());
    }

    /**
     * Test method for {@link org.talend.components.salesforce.runtime.SalesforceSourceOrSink#getConnectionProperties()}
     * .
     */
    @Test
    public void testGetConnectionProperties() {
        // using SalesforceConnectionProperties
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, scp);
        assertEquals(scp, salesforceSourceOrSink.getConnectionProperties());

        // using SalesforceConnectionProperties
        SalesforceConnectionModuleProperties scmp = new SalesforceConnectionModuleProperties(null);
        salesforceSourceOrSink.initialize(null, scmp);
        assertEquals(scmp.connection, salesforceSourceOrSink.getConnectionProperties());
    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSourceOrSink#getSchemaNames(org.talend.components.api.adaptor.Adaptor)}
     * .
     */
    @Test
    public void testGetSchemaNames() {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, scp);
        List<NamedThing> schemaNames = salesforceSourceOrSink.getSchemaNames(null);
        assertTrue(schemaNames.size() > 50);
    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSourceOrSink#setupSchemaElement(com.sforce.soap.partner.Field, org.talend.daikon.schema.SchemaElement)}
     * .
     */
    @Test
    public void testSetupSchemaElement() {
        Field field = new Field();
        Schema newSchema = SchemaFactory.newSchema();
        field.setType(FieldType._boolean);
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        newSchema.setRoot(SchemaFactory.newSchemaElement(Type.SCHEMA, ""));
        assertEquals(Type.SCHEMA, newSchema.getRoot().getType());
        //
        salesforceSourceOrSink.setupSchemaElement(field, newSchema.getRoot());
        assertEquals(Type.BOOLEAN, newSchema.getRoot().getType());
        //
        field.setType(FieldType._int);
        salesforceSourceOrSink.setupSchemaElement(field, newSchema.getRoot());
        assertEquals(Type.INT, newSchema.getRoot().getType());
        //
        field.setType(FieldType.date);
        salesforceSourceOrSink.setupSchemaElement(field, newSchema.getRoot());
        assertEquals(Type.DATE, newSchema.getRoot().getType());
        //
        field.setType(FieldType.datetime);
        salesforceSourceOrSink.setupSchemaElement(field, newSchema.getRoot());
        assertEquals(Type.DATETIME, newSchema.getRoot().getType());
        //
        field.setType(FieldType._double);
        salesforceSourceOrSink.setupSchemaElement(field, newSchema.getRoot());
        assertEquals(Type.DOUBLE, newSchema.getRoot().getType());
        //
        field.setType(FieldType.currency);
        salesforceSourceOrSink.setupSchemaElement(field, newSchema.getRoot());
        assertEquals(Type.DECIMAL, newSchema.getRoot().getType());

    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSourceOrSink#getSchema(org.talend.components.api.adaptor.Adaptor, java.lang.String)}
     * .
     */
    @Test
    public void testGetSchema() {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, scp);
        List<NamedThing> schemaNames = salesforceSourceOrSink.getSchemaNames(null);
        assertFalse(schemaNames.isEmpty());
        // check for not existing module
        try {
            Schema schema = salesforceSourceOrSink.getSchema(null, "module that does not exists");
            fail("Should have throw an exception when not finding the module");
        } catch (ComponentException ce) {
            // exception expected so ignor
        }
    }

    private SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, boolean addQuotes) {
        if (props == null) {
            props = (SalesforceConnectionProperties) new SalesforceConnectionProperties("foo").init();
        }
        ComponentProperties userPassword = (ComponentProperties) props.getProperty("userPassword");
        ((Property) userPassword.getProperty("userId")).setValue(addQuotes ? "\"" + userId + "\"" : userId);
        ((Property) userPassword.getProperty("password")).setValue(addQuotes ? "\"" + password + "\"" : password);
        ((Property) userPassword.getProperty("securityKey")).setValue(addQuotes ? "\"" + securityKey + "\"" : securityKey);
        return props;
    }

}
