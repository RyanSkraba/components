package org.talend.components.processing.runtime.typeconverter;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties;

public class TypeConverterUtilsTest {

    private final Schema inputSchemaL = SchemaBuilder.record("inputRowL") //
            .fields() //
            .name("l").type().optional().stringType() //
            .endRecord();

    private final Schema inputSchemaListOfL = SchemaBuilder.array().items(inputSchemaL);

    private final Schema inputSchemaJK = SchemaBuilder.record("inputRowJK") //
            .fields() //
            .name("j").type(inputSchemaListOfL).noDefault() //
            .name("k").type().optional().stringType() //
            .endRecord();

    private final Schema inputSchemaDE = SchemaBuilder.record("inputRowDE") //
            .fields() //
            .name("d").type(inputSchemaJK).noDefault() //
            .name("e").type().optional().stringType() //
            .endRecord();

    private final Schema inputSchemaHI = SchemaBuilder.record("inputRowHI") //
            .fields() //
            .name("h").type().optional().stringType() //
            .name("i").type().optional().stringType() //
            .endRecord();

    private final Schema inputSchemaListOfHI = SchemaBuilder.array().items(inputSchemaHI);

    private final Schema inputSchemaFG = SchemaBuilder.record("inputRowFG") //
            .fields() //
            .name("f").type().optional().stringType() //
            .name("g").type(inputSchemaListOfHI).noDefault() //
            .endRecord();

    private final Schema inputSchemaXY = SchemaBuilder.record("inputRowXY") //
            .fields() //
            .name("x").type().stringType().noDefault() //
            .name("y").type(inputSchemaDE).noDefault() //
            .endRecord();

    private final Schema inputSchemaListM = SchemaBuilder.array().items().stringType();

    private final Schema inputParentSchema = SchemaBuilder.record("inputParentRow") //
            .fields() //
            .name("a").type().stringType().noDefault() //
            .name("b").type(inputSchemaXY).noDefault() //
            .name("c").type(inputSchemaFG).noDefault() //
            .name("m").type(inputSchemaListM).noDefault() //
            .endRecord();

    private final Schema dateLogicalType = LogicalTypes.date().addToSchema(Schema.create(Schema.Type.INT));

    private final Schema timeLogicalType = LogicalTypes.timeMillis().addToSchema(Schema.create(Schema.Type.INT));

    private final Schema dateTimeLogicalType = LogicalTypes.timestampMillis().addToSchema(Schema.create(Schema.Type.LONG));

    /**
     * {"l":"l1"}
     */
    private final GenericRecord inputRecordL1 = new GenericRecordBuilder(inputSchemaL) //
            .set("l", "l1") //
            .build();

    /**
     * {"l":"l2"}
     */
    private final GenericRecord inputRecordL2 = new GenericRecordBuilder(inputSchemaL) //
            .set("l", "l2") //
            .build();

    /**
     * [{"l":"l1"},{"l":"l2"}]
     */
    private final List<GenericRecord> listInputRecordL = Arrays.asList(inputRecordL1, inputRecordL2);

    /**
     * {"j": [{"l":"l1"},{"l":"l2"}], "k": "k1;k2"}
     */
    private final GenericRecord inputRecordJK = new GenericRecordBuilder(inputSchemaJK) //
            .set("j", listInputRecordL) //
            .set("k", "k1;k2") //
            .build();

    /**
     * {"d": {"j": [{"l":"l1"},{"l":"l2"}], "k": "k1;k2"}, "e": "e"}
     */
    private final GenericRecord inputRecordDE = new GenericRecordBuilder(inputSchemaDE) //
            .set("d", inputRecordJK) //
            .set("e", "e") //
            .build();

    /**
     * {"h": "h1", "i": "i2"}
     */
    private final GenericRecord inputRecordHI1 = new GenericRecordBuilder(inputSchemaHI) //
            .set("h", "h1") //
            .set("i", "i2") //
            .build();

    /**
     * {"h": "h2", "i": "i1"}
     */
    private final GenericRecord inputRecordHI2 = new GenericRecordBuilder(inputSchemaHI) //
            .set("h", "h2") //
            .set("i", "i1") //
            .build();

    /**
     * [{"h": "h1", "i": "i2"}, {"h": "h2", "i": "i1"}]
     */
    private final List<GenericRecord> listInputRecordG = Arrays.asList(inputRecordHI1, inputRecordHI2);

    /**
     * {"f": "f", "g": [{"h": "h1", "i": "i2"}, {"h": "h2", "i": "i1"}]}
     */
    private final GenericRecord inputRecordFG = new GenericRecordBuilder(inputSchemaFG) //
            .set("f", "f") //
            .set("g", listInputRecordG) // inputRecordHI
            .build();

    /**
     * {"x": "x1;x2", "y": {"d": {"j": [{"l":"l1"},{"l":"l2"}], "k": "k1;k2"}, "e": "e"}}
     */
    private final GenericRecord inputRecordXY = new GenericRecordBuilder(inputSchemaXY) //
            .set("x", "x1;x2") //
            .set("y", inputRecordDE) // listDE
            .build();

    /**
     * ["m1", "m2", "m3"]
     */
    private final List<String> listInputRecordM = Arrays.asList("m1", "m2", "m3");

    /**
     * { "a": "0", "b": {"x": "x1;x2", "y": {"d": {"j": [{"l":"l1"},{"l":"l2"}], "k": "k1;k2"}, "e": "e"}}, "c": {"f":
     * "f", "g": [{"h": "h1", "i": "i2"}, {"h": "h2", "i": "i1"}]}, "m": ["m1", "m2", "m3"]}
     */
    private final GenericRecord inputParentRecord = new GenericRecordBuilder(inputParentSchema) //
            .set("a", "0") //
            .set("b", inputRecordXY) //
            .set("c", inputRecordFG) //
            .set("m", listInputRecordM) //
            .build();

    private final Schema listSchemas = SchemaBuilder.array().items(inputSchemaListM);

    private final List<List<String>> listRecords = Arrays.asList(listInputRecordM);

    private final Schema listOfListSchema = SchemaBuilder.record("listOfListRow") //
            .fields() //
            .name("parentList").type(listSchemas).noDefault() //
            .endRecord();

    private final GenericRecord listOfListRecord = new GenericRecordBuilder(listOfListSchema) //
            .set("parentList", listRecords) //
            .build();

    public void testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes outputType, String expected) {
        String[] aPath = { "a" };
        Stack<String> stackPath = new Stack<String>();
        stackPath.addAll(Arrays.asList(aPath));
        Schema newSchema = TypeConverterUtils.convertSchema(inputParentSchema, stackPath, outputType, null);
        Assert.assertEquals(expected, newSchema.toString());
    }

    @Test
    public void testConvertSchemaToBoolean() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Boolean,
                "{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"boolean\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToDouble() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Double,
                "{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"double\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToFloat() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Float,
                "{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"float\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToInteger() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Integer,
                "{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"int\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToLong() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Long,
                "{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"long\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToTime() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Time,
                "{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":{\"type\":\"int\",\"logicalType\":\"time-millis\"}},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToDateTime() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.DateTime,
                "{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchema() {

        String[] aPath = { "a" };
        Stack<String> stackPath = new Stack<String>();
        stackPath.addAll(Arrays.asList(aPath));

        // String to int
        String[] path1 = { "a" };
        Stack<String> stackPath1 = new Stack<String>();
        stackPath1.addAll(Arrays.asList(path1));
        Schema newSchema1 = TypeConverterUtils.convertSchema(inputParentSchema, stackPath1,
                TypeConverterProperties.TypeConverterOutputTypes.Integer, null);

        Schema expectedParentSchema1 = SchemaBuilder.record("inputParentRow") //
                .fields() //
                .name("a").type().intType().noDefault() //
                .name("b").type(inputSchemaXY).noDefault() //
                .name("c").type(inputSchemaFG).noDefault() //
                .name("m").type(inputSchemaListM).noDefault() //
                .endRecord();

        Assert.assertEquals(expectedParentSchema1.toString(), newSchema1.toString());

        // String to float
        String[] path2 = { "b", "x" };
        Stack<String> stackPath2 = new Stack<String>();
        List<String> pathSteps2 = Arrays.asList(path2);
        Collections.reverse(pathSteps2);
        stackPath2.addAll(pathSteps2);
        Schema newSchema2 = TypeConverterUtils.convertSchema(inputParentSchema, stackPath2,
                TypeConverterProperties.TypeConverterOutputTypes.Float, null);

        Schema expectedSchemaXY = SchemaBuilder.record("inputRowXY") //
                .fields() //
                .name("x").type().floatType().noDefault() //
                .name("y").type(inputSchemaDE).noDefault() //
                .endRecord();

        Schema expectedParentSchema2 = SchemaBuilder.record("inputParentRow") //
                .fields() //
                .name("a").type().stringType().noDefault() //
                .name("b").type(expectedSchemaXY).noDefault() //
                .name("c").type(inputSchemaFG).noDefault() //
                .name("m").type(inputSchemaListM).noDefault() //
                .endRecord();

        Assert.assertEquals(expectedParentSchema2.toString(), newSchema2.toString());

    }

    @Test
    public void testCopyFieldsValues() {
        Schema intSchema = SchemaBuilder.record("intSchema").fields().name("a").type().intType().noDefault().endRecord();
        GenericRecord intRecord = new GenericRecordBuilder(intSchema).set("a", 1).build();

        Schema stringSchema = SchemaBuilder.record("intSchema").fields().name("a").type().stringType().noDefault().endRecord();
        GenericRecordBuilder stringRecordBuilder = new GenericRecordBuilder(stringSchema).set("a", "s");
        TypeConverterUtils.copyFieldsValues(intRecord, stringRecordBuilder);
        GenericRecord stringRecord = stringRecordBuilder.build();
        Assert.assertEquals(intRecord.get("a"), stringRecord.get("a"));
    }

    /**
     * Performs a type conversion with the following information.
     *
     * @param inputSchema The Avro schema that corresponds to the input object to convert.
     * @param input The input object to convert.
     * @param outputType The output type expected.
     * @param inputFormat The format to use in the conversion, or null if none.
     * @param outputClass The expected class of the output object.
     * @return The converted value.
     */
    public <T> T testConvertValue(Schema inputSchema, Object input, TypeConverterProperties.TypeConverterOutputTypes outputType,
            String inputFormat, Class<T> outputClass) {
        // Create a temporary schema to use for converting the value.
        Schema recordSchema = SchemaBuilder.record("TestConvertValueRecord") //
                .fields() //
                .name("instance").type(inputSchema).noDefault().endRecord();
        // And a record that contains the value to convert.
        GenericRecordBuilder outputRecordBuilder = new GenericRecordBuilder(recordSchema).set("instance", input);
        Stack<String> converterPath = new Stack<String>();
        converterPath.add("instance");
        // Do the conversion.
        TypeConverterUtils.convertValue(recordSchema, outputRecordBuilder, converterPath, outputType, inputFormat);
        GenericRecord outputRecord = outputRecordBuilder.build();
        // And check.
        assertThat(outputRecord.get(0), anyOf(nullValue(), instanceOf(outputClass)));
        return (T) outputRecord.get(0);
    }

    @Test
    public void testConvertValueToBoolean() {
        boolean conv = testConvertValue(Schema.create(Schema.Type.STRING), "false",
                TypeConverterProperties.TypeConverterOutputTypes.Boolean, null, Boolean.class);
        assertThat(conv, is(false));
    }

    @Test
    public void testConvertValueToDouble() {
        Double conv = testConvertValue(Schema.create(Schema.Type.STRING), "2.5",
                TypeConverterProperties.TypeConverterOutputTypes.Double, null, Double.class);
        assertThat(conv, closeTo(2.5, 1e-10));
    }

    @Test
    public void testConvertValueToFloat() {
        testConvertValue(Schema.create(Schema.Type.STRING), "3.5", TypeConverterProperties.TypeConverterOutputTypes.Float, null,
                Float.class);
    }

    @Test
    public void testConvertValueToInteger() {
        testConvertValue(Schema.create(Schema.Type.STRING), "1", TypeConverterProperties.TypeConverterOutputTypes.Integer, null,
                Integer.class);
    }

    @Test
    public void testConvertValueToIntegerWithFormat() {
        testConvertValue(Schema.create(Schema.Type.STRING), "0.5", TypeConverterProperties.TypeConverterOutputTypes.Integer, "#",
                Integer.class);
    }

    @Test
    public void testConvertValueToLong() {
        testConvertValue(Schema.create(Schema.Type.STRING), "2", TypeConverterProperties.TypeConverterOutputTypes.Long, null,
                Long.class);
    }

    @Test
    public void testConvertValueToString() {
        testConvertValue(Schema.create(Schema.Type.INT), 1, TypeConverterProperties.TypeConverterOutputTypes.String, null,
                String.class);
    }

    // Number conversion tests ---------------------------------------

    @Test
    public void testConvertStringToInt() {
        // No format
        int conv = testConvertValue(Schema.create(Schema.Type.STRING), "123",
                TypeConverterProperties.TypeConverterOutputTypes.Integer, null, Integer.class);
        assertThat(conv, is(123));

        // One format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "#123",
                TypeConverterProperties.TypeConverterOutputTypes.Integer, "'#'#", Integer.class);
        assertThat(conv, is(123));

        // Another format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "123,456.78",
                TypeConverterProperties.TypeConverterOutputTypes.Integer, "#,###.##", Integer.class);
        assertThat(conv, is(123456));
    }

    @Test
    public void testConvertIntToString() {
        // No format
        String conv = testConvertValue(Schema.create(Schema.Type.INT), 123,
                TypeConverterProperties.TypeConverterOutputTypes.String, null, String.class);
        assertThat(conv, is("123"));

        // One format
        conv = testConvertValue(Schema.create(Schema.Type.INT), 123, TypeConverterProperties.TypeConverterOutputTypes.String,
                "'#'#", String.class);
        assertThat(conv, is("#123"));

        // Another format
        conv = testConvertValue(Schema.create(Schema.Type.INT), 123456, TypeConverterProperties.TypeConverterOutputTypes.String,
                "#,###.00", String.class);
        assertThat(conv, is("123,456.00"));
    }

    @Test
    public void testConvertStringToLong() {
        // No format
        long conv = testConvertValue(Schema.create(Schema.Type.STRING), "123",
                TypeConverterProperties.TypeConverterOutputTypes.Long, null, Long.class);
        assertThat(conv, is(123L));

        // One format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "#123", TypeConverterProperties.TypeConverterOutputTypes.Long,
                "'#'#", Long.class);
        assertThat(conv, is(123L));

        // Another format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "123,456.78",
                TypeConverterProperties.TypeConverterOutputTypes.Long, "#,###.00", Long.class);
        assertThat(conv, is(123456L));
    }

    @Test
    public void testConvertLongToString() {
        // No format
        String conv = testConvertValue(Schema.create(Schema.Type.LONG), 123L,
                TypeConverterProperties.TypeConverterOutputTypes.String, null, String.class);
        assertThat(conv, is("123"));

        // One format
        conv = testConvertValue(Schema.create(Schema.Type.LONG), 123L, TypeConverterProperties.TypeConverterOutputTypes.String,
                "'#'#", String.class);
        assertThat(conv, is("#123"));

        // Another format
        conv = testConvertValue(Schema.create(Schema.Type.LONG), 123456L, TypeConverterProperties.TypeConverterOutputTypes.String,
                "#,###.00", String.class);
        assertThat(conv, is("123,456.00"));
    }

    @Test
    public void testConvertStringToFloat() {
        // No format
        float conv = testConvertValue(Schema.create(Schema.Type.STRING), "123",
                TypeConverterProperties.TypeConverterOutputTypes.Float, null, Float.class);
        assertThat(conv, is(123f));

        // With one format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "#123", TypeConverterProperties.TypeConverterOutputTypes.Float,
                "'#'#", Float.class);
        assertThat(conv, is(123f));

        // With another format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "123,456.78",
                TypeConverterProperties.TypeConverterOutputTypes.Float, "#,###.00", Float.class);
        assertThat((double) conv, closeTo(123456.78, 1e-2));
    }

    @Test
    public void testConvertFloatToString() {
        // No format
        String conv = testConvertValue(Schema.create(Schema.Type.FLOAT), 123f,
                TypeConverterProperties.TypeConverterOutputTypes.String, null, String.class);
        assertThat(conv, is("123.0"));

        // One format
        conv = testConvertValue(Schema.create(Schema.Type.FLOAT), 123f, TypeConverterProperties.TypeConverterOutputTypes.String,
                "'#'#", String.class);
        assertThat(conv, is("#123"));

        // Another format
        conv = testConvertValue(Schema.create(Schema.Type.FLOAT), 123456.78f,
                TypeConverterProperties.TypeConverterOutputTypes.String, "#,###.00", String.class);
        assertThat(conv, is("123,456.78"));
    }

    @Test
    public void testConvertStringToDouble() {
        // No format
        double conv = testConvertValue(Schema.create(Schema.Type.STRING), "123",
                TypeConverterProperties.TypeConverterOutputTypes.Double, null, Double.class);
        assertThat(conv, is(123d));

        // With one format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "#123",
                TypeConverterProperties.TypeConverterOutputTypes.Double, "'#'#", Double.class);
        assertThat(conv, is(123d));

        // With another format
        conv = testConvertValue(Schema.create(Schema.Type.STRING), "123,456.78",
                TypeConverterProperties.TypeConverterOutputTypes.Double, "#,###.00", Double.class);
        assertThat(conv, closeTo(123456.78d, 1e-10));
    }

    @Test
    public void testConvertDoubleToString() {
        // No format
        String conv = testConvertValue(Schema.create(Schema.Type.DOUBLE), 123d,
                TypeConverterProperties.TypeConverterOutputTypes.String, null, String.class);
        assertThat(conv, is("123.0"));

        // One format
        conv = testConvertValue(Schema.create(Schema.Type.DOUBLE), 123d, TypeConverterProperties.TypeConverterOutputTypes.String,
                "'#'#", String.class);
        assertThat(conv, is("#123"));

        // Another format
        conv = testConvertValue(Schema.create(Schema.Type.DOUBLE), 123456.78d,
                TypeConverterProperties.TypeConverterOutputTypes.String, "#,###.00", String.class);
        assertThat(conv, is("123,456.78"));
    }

    // Date-oriented conversion tests ---------------------------------------

    @Test
    public void testConvertDateToString() {
        String conv = testConvertValue(dateLogicalType, 17498, TypeConverterProperties.TypeConverterOutputTypes.String,
                "yyyy.MM.dd G", String.class);
        assertThat(conv, is("2017.11.28 AD"));
    }

    @Test
    public void testConvertTimeToString() {
        String conv = testConvertValue(timeLogicalType, 45862123, TypeConverterProperties.TypeConverterOutputTypes.String,
                "HH:mm:ss[ z]", String.class);
        assertThat(conv, is("12:44:22"));
    }

    @Test
    public void testConvertDateTimeToString() {
        String conv = testConvertValue(dateTimeLogicalType, 1511873062123L,
                TypeConverterProperties.TypeConverterOutputTypes.String, "yyyy.MM.dd G 'at' HH:mm:ss[ z]", String.class);
        assertThat(conv, is("2017.11.28 AD at 12:44:22"));
    }

    @Test
    public void testConvertDateToStringNoFormat() {
        String conv = testConvertValue(dateLogicalType, 17498, TypeConverterProperties.TypeConverterOutputTypes.String, "",
                String.class);
        assertThat(conv, is("2017-11-28"));
    }

    @Test
    public void testConvertTimeToStringNoFormat() {
        String conv = testConvertValue(timeLogicalType, 45862123, TypeConverterProperties.TypeConverterOutputTypes.String, "",
                String.class);
        assertThat(conv, is("12:44:22.123"));
    }

    @Test
    public void testConvertDateTimeToStringNoFormat() {
        String conv = testConvertValue(dateTimeLogicalType, 1511873062123L,
                TypeConverterProperties.TypeConverterOutputTypes.String, "", String.class);
        assertThat(conv, is("2017-11-28T12:44:22.123"));
    }

    @Test
    public void testConvertStringToDate() {
        int conv = testConvertValue(Schema.create(Schema.Type.STRING), "2017.11.28 AD",
                TypeConverterProperties.TypeConverterOutputTypes.Date, "yyyy.MM.dd G", Integer.class);
        assertThat(conv, is(17498));
    }

    @Test
    public void testConvertStringToTime() {
        int conv = testConvertValue(Schema.create(Schema.Type.STRING), "12:44:22",
                TypeConverterProperties.TypeConverterOutputTypes.Time, "HH:mm:ss[ z]", Integer.class);
        assertThat(conv, is(45862000));
    }

    @Test
    public void testConvertStringToDateTime() {
        long conv = testConvertValue(Schema.create(Schema.Type.STRING), "2017.11.28 AD at 12:44:22",
                TypeConverterProperties.TypeConverterOutputTypes.DateTime, "yyyy.MM.dd G 'at' HH:mm:ss[ z]", Long.class);
        assertThat(conv, is(1511873062000L));
    }

    @Test
    public void testConvertStringToDateTimeWithFormatOnlyDate() {
        long conv = testConvertValue(Schema.create(Schema.Type.STRING), "28/11/2017",
                TypeConverterProperties.TypeConverterOutputTypes.DateTime, "dd/MM/yyyy", Long.class);
        assertThat(conv, is(1511827200000L));
    }

    @Test
    public void testConvertStringToDateTimeWithFormatOnlyTime() {
        long conv = testConvertValue(Schema.create(Schema.Type.STRING), "12:44:22",
                TypeConverterProperties.TypeConverterOutputTypes.DateTime, "HH:mm:ss", Long.class);
        assertThat(conv, is(45862000L));
    }

    @Test
    public void testConvertStringToDateNoFormat() {
        int conv = testConvertValue(Schema.create(Schema.Type.STRING), "2017-11-28",
                TypeConverterProperties.TypeConverterOutputTypes.Date, "", Integer.class);
        assertThat(conv, is(17498));
    }

    @Test
    public void testConvertStringToTimeNoFormat() {
        int conv = testConvertValue(Schema.create(Schema.Type.STRING), "12:44:22.123",
                TypeConverterProperties.TypeConverterOutputTypes.Time, "", Integer.class);
        assertThat(conv, is(45862123));
    }

    @Test
    public void testConvertStringToDateTimeNoFormat() {
        long conv = testConvertValue(Schema.create(Schema.Type.STRING), "2017-11-28T12:44:22.123",
                TypeConverterProperties.TypeConverterOutputTypes.DateTime, "", Long.class);
        assertThat(conv, is(1511873062123L));
    }

    @Test
    public void testConvertIntValueToDate() {
        int conv = testConvertValue(Schema.create(Schema.Type.INT), 17498, TypeConverterProperties.TypeConverterOutputTypes.Date,
                "", Integer.class);
        assertThat(conv, is(17498));
    }

    @Test
    public void testConvertIntValueToTime() {
        int conv = testConvertValue(Schema.create(Schema.Type.INT), 45862123,
                TypeConverterProperties.TypeConverterOutputTypes.Time, "", Integer.class);
        assertThat(conv, is(45862123));
    }

    @Test
    public void testConvertIntValueToDateTime() {
        long conv = testConvertValue(Schema.create(Schema.Type.LONG), 1511873062123L,
                TypeConverterProperties.TypeConverterOutputTypes.DateTime, "", Long.class);
        assertThat(conv, is(1511873062123L));
    }

    @Test
    public void testConvertNullableDateToString() {
        String conv = testConvertValue(Schema.createUnion(dateLogicalType, Schema.create(Schema.Type.NULL)), 17498,
                TypeConverterProperties.TypeConverterOutputTypes.String, "yyyy.MM.dd G", String.class);
        assertThat(conv, is("2017.11.28 AD"));

        conv = testConvertValue(Schema.createUnion(dateLogicalType, Schema.create(Schema.Type.NULL)), null,
                TypeConverterProperties.TypeConverterOutputTypes.String, "yyyy.MM.dd G", String.class);
        assertThat(conv, nullValue());
    }

    @Test
    public void testGetPathSteps() {
        String pathSteps = ".a.b";
        Stack<String> result = TypeConverterUtils.getPathSteps(pathSteps);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("a", result.pop());
    }
}
