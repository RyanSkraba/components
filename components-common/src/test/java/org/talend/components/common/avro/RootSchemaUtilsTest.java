package org.talend.components.common.avro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit-tests for class {@link RootSchemaUtils}
 */
public final class RootSchemaUtilsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link RootSchemaUtils#createRootSchema(Schema, Schema)} returns {@link Schema} of type {@link Type#RECORD}
     * with name "Root", which has 2 fields with names: "Main" and "OutOfBand"
     */
    @Test
    public void testCreateRootSchema() {
        String expectedSchemaName = "Root";
        Type expectedType = Type.RECORD;
        String expectedMainFieldName = "Main";
        String expectedOutOfBandFieldName = "OutOfBand";

        Schema mainSchema = SchemaBuilder.builder().record("Main").fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.builder().record("OutOfBand").fields().endRecord(); //

        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);
        assertNotNull(rootSchema);

        String actualSchemaName = rootSchema.getName();
        assertEquals(expectedSchemaName, actualSchemaName);

        Type actualType = rootSchema.getType();
        assertEquals(expectedType, actualType);

        List<Field> fields = rootSchema.getFields();
        assertThat(fields, hasSize(2));

        Field main = fields.get(0);
        String actualMainFieldName = main.name();
        assertEquals(expectedMainFieldName, actualMainFieldName);

        Schema actualMainSchema = main.schema();
        assertEquals(mainSchema, actualMainSchema);

        Field outOfBand = fields.get(1);
        String actualOutOfBandFieldName = outOfBand.name();
        assertEquals(expectedOutOfBandFieldName, actualOutOfBandFieldName);

        Schema actualOutOfBandSchema = outOfBand.schema();
        assertEquals(outOfBandSchema, actualOutOfBandSchema);
    }

    /**
     * Checks {@link RootSchemaUtils#createRootSchema(Schema, Schema)} throws {@link IllegalArgumentException}
     * with message "Input schemas should be not null"
     * in case input arguments are null
     */
    @Test
    public void testCreateRootSchemaIllegalArgumentException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input schemas should be not null");

        Schema mainSchema = null;
        Schema outOfBandSchema = null;

        RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);
    }

    /**
     * Checks {@link RootSchemaUtils#isRootSchema(Schema)} returns <code>false</code>, when null is passed as argument
     */
    @Test
    public void testIsRootSchemaNull() {
        boolean actual = RootSchemaUtils.isRootSchema(null);
        assertFalse(actual);
    }

    /**
     * Checks {@link RootSchemaUtils#isRootSchema(Schema)} returns <code>false</code>, when not Root schema is passed
     * Neither schema name nor field names are incorrect
     */
    @Test
    public void testIsRootSchemaNotRoot() {
        Schema notRoot = SchemaBuilder.builder().record("NotRoot").fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .endRecord(); //

        boolean actual = RootSchemaUtils.isRootSchema(notRoot);
        assertFalse(actual);
    }

    /**
     * Checks {@link RootSchemaUtils#isRootSchema(Schema)} returns <code>false</code>, when not Root schema is passed
     * Schema has correct name, but it's type is not {@link Type#RECORD}
     */
    @Test
    public void testIsRootSchemaRootWithWrongType() {
        Schema wrongTypeSchema = SchemaBuilder.builder().fixed("Root").size(16); //

        boolean actual = RootSchemaUtils.isRootSchema(wrongTypeSchema);
        assertFalse(actual);
    }

    /**
     * Checks {@link RootSchemaUtils#isRootSchema(Schema)} returns <code>false</code>, when not Root schema is passed
     * Schema name is correct, but field names are incorrect
     */
    @Test
    public void testIsRootSchemaRootWithWrongFields() {
        Schema rootWithWrongFields = SchemaBuilder.builder().record("Root").fields() //
                .name("Main").type().intType().noDefault() //
                .name("NotOutOfBand").type().stringType().noDefault() //
                .endRecord(); //

        boolean actual = RootSchemaUtils.isRootSchema(rootWithWrongFields);
        assertFalse(actual);

        rootWithWrongFields = SchemaBuilder.builder().record("Root").fields() //
                .name("NotMain").type().intType().noDefault() //
                .name("OutOfBand").type().stringType().noDefault() //
                .endRecord(); //

        actual = RootSchemaUtils.isRootSchema(rootWithWrongFields);
        assertFalse(actual);
    }

    /**
     * Checks {@link RootSchemaUtils#isRootSchema(Schema)} returns <code>true</code>, when Root schema is passed
     */
    @Test
    public void testIsRootSchemaRoot() {
        Schema root = SchemaBuilder.builder().record("Root").fields() //
                .name("Main").type().intType().noDefault() //
                .name("OutOfBand").type().intType().noDefault() //
                .endRecord(); //

        boolean actual = RootSchemaUtils.isRootSchema(root);
        assertTrue(actual);
    }

    /**
     * Checks {@link RootSchemaUtils#getMainSchema(Schema)} throws {@link IllegalArgumentException} in case of
     * not Root schema is passed with following message "Input schema should be Root schema"
     */
    @Test
    public void testGetMainSchemaIllegalArgumentException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input schema should be Root schema");

        Schema notRoot = SchemaBuilder.builder().fixed("Root").size(16);

        RootSchemaUtils.getMainSchema(notRoot);
    }

    /**
     * Checks {@link RootSchemaUtils#getMainSchema(Schema)} returns correct Main schema retrieved from incoming
     * Root schema
     */
    @Test
    public void testGetMainSchema() {
        Schema mainSchema = SchemaBuilder.builder().record("Main").fields() //
                .name("id").type().intType().noDefault() //
                .endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.builder().record("EmptySchema").fields().endRecord(); //$NON-NLS-1$

        Schema rootSchema = SchemaBuilder.record("Root").fields() //$NON-NLS-1$
                .name("Main").type(mainSchema).noDefault() // $NON-NLS-1$
                .name("OutOfBand").type(outOfBandSchema).noDefault() // $NON-NLS-1$
                .endRecord(); //

        Schema actualMainSchema = RootSchemaUtils.getMainSchema(rootSchema);
        assertEquals(mainSchema, actualMainSchema);
    }

    /**
     * Checks {@link RootSchemaUtils#getOutOfBandSchema(Schema)} throws {@link IllegalArgumentException} in case of
     * not Root schema is passed with following message "Input schema should be Root schema"
     */
    @Test
    public void testGetOutOfBandSchemaIllegalArgumentException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input schema should be Root schema");

        Schema notRoot = SchemaBuilder.builder().fixed("Root").size(16);

        RootSchemaUtils.getMainSchema(notRoot);
    }

    /**
     * Checks {@link RootSchemaUtils#getOutOfBandSchema(Schema)} returns correct Out of band schema retrieved from incoming
     * Root schema
     */
    @Test
    public void testGetOutOfBandSchema() {
        Schema mainSchema = SchemaBuilder.builder().record("Main").fields() //
                .name("id").type().intType().noDefault() //
                .endRecord(); //

        Schema outOfBandSchema = SchemaBuilder.builder().record("EmptySchema").fields().endRecord(); //$NON-NLS-1$

        Schema rootSchema = SchemaBuilder.record("Root").fields() //$NON-NLS-1$
                .name("Main").type(mainSchema).noDefault() // $NON-NLS-1$
                .name("OutOfBand").type(outOfBandSchema).noDefault() // $NON-NLS-1$
                .endRecord(); //

        Schema actualOutOfBandSchema = RootSchemaUtils.getOutOfBandSchema(rootSchema);
        assertEquals(outOfBandSchema, actualOutOfBandSchema);
    }
}
