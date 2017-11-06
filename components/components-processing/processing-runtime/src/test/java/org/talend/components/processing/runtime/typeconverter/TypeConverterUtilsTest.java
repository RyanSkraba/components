package org.talend.components.processing.runtime.typeconverter;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

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


    public void testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes outputType, String expected){
        String[] aPath = {"a"};
        Stack<String> stackPath = new Stack<String>();
        stackPath.addAll(Arrays.asList(aPath));
        Schema newSchema = TypeConverterUtils.convertSchema(inputParentSchema, stackPath, outputType, null);
        Assert.assertEquals(expected, newSchema.toString());
    }

    @Test
    public void testConvertSchemaToBoolean() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Boolean,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"boolean\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToDecimal() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Decimal,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":20,\"scale\":4}},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToDouble() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Double,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"double\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToFloat() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Float,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"float\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToInteger() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Integer,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"int\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToLong() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Long,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":\"long\"},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToTime() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.Time,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":{\"type\":\"int\",\"logicalType\":\"time-millis\"}},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchemaToDateTime() {
        testConvertSchema(TypeConverterProperties.TypeConverterOutputTypes.DateTime,"{\"type\":\"record\",\"name\":\"inputParentRow\",\"fields\":[{\"name\":\"a\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}},{\"name\":\"b\",\"type\":{\"type\":\"record\",\"name\":\"inputRowXY\",\"fields\":[{\"name\":\"x\",\"type\":\"string\"},{\"name\":\"y\",\"type\":{\"type\":\"record\",\"name\":\"inputRowDE\",\"fields\":[{\"name\":\"d\",\"type\":{\"type\":\"record\",\"name\":\"inputRowJK\",\"fields\":[{\"name\":\"j\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowL\",\"fields\":[{\"name\":\"l\",\"type\":[\"null\",\"string\"],\"default\":null}]}}},{\"name\":\"k\",\"type\":[\"null\",\"string\"],\"default\":null}]}},{\"name\":\"e\",\"type\":[\"null\",\"string\"],\"default\":null}]}}]}},{\"name\":\"c\",\"type\":{\"type\":\"record\",\"name\":\"inputRowFG\",\"fields\":[{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"g\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"inputRowHI\",\"fields\":[{\"name\":\"h\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"i\",\"type\":[\"null\",\"string\"],\"default\":null}]}}}]}},{\"name\":\"m\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    }

    @Test
    public void testConvertSchema() {

        String[] aPath = {"a"};
        Stack<String> stackPath = new Stack<String>();
        stackPath.addAll(Arrays.asList(aPath));


        // String to int
        String[] path1 = {"a"};
        Stack<String> stackPath1 = new Stack<String>();
        stackPath1.addAll(Arrays.asList(path1));
        Schema newSchema1 = TypeConverterUtils.convertSchema(inputParentSchema, stackPath1, TypeConverterProperties.TypeConverterOutputTypes.Integer, null);

        Schema expectedParentSchema1 = SchemaBuilder.record("inputParentRow") //
                .fields() //
                .name("a").type().intType().noDefault() //
                .name("b").type(inputSchemaXY).noDefault() //
                .name("c").type(inputSchemaFG).noDefault() //
                .name("m").type(inputSchemaListM).noDefault() //
                .endRecord();

        Assert.assertEquals(expectedParentSchema1.toString(), newSchema1.toString());

        // String to float
        String[] path2 = {"b", "x"};
        Stack<String> stackPath2 = new Stack<String>();
        List<String> pathSteps2 = Arrays.asList(path2);
        Collections.reverse(pathSteps2);
        stackPath2.addAll(pathSteps2);
        Schema newSchema2 = TypeConverterUtils.convertSchema(inputParentSchema, stackPath2, TypeConverterProperties.TypeConverterOutputTypes.Float, null);

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
        Schema intSchema = SchemaBuilder.record("intSchema")
                .fields()
                .name("a").type().intType().noDefault()
                .endRecord();
        GenericRecord intRecord = new GenericRecordBuilder(intSchema)
                .set("a", 1)
                .build();

        Schema stringSchema = SchemaBuilder.record("intSchema")
                .fields()
                .name("a").type().stringType().noDefault()
                .endRecord();
        GenericRecordBuilder stringRecordBuilder = new GenericRecordBuilder(stringSchema)
                .set("a", "s");
        TypeConverterUtils.copyFieldsValues(intRecord, stringRecordBuilder);
        GenericRecord stringRecord = stringRecordBuilder.build();
        Assert.assertEquals(intRecord.get("a"), stringRecord.get("a"));
    }


    public void testConvertValue(Object input, TypeConverterProperties.TypeConverterOutputTypes outputType, String inputFormat, Class outputClass) {
        GenericRecordBuilder outputRecordBuilder = new GenericRecordBuilder(inputSchemaL).set("l", input);
        Stack<String> converterPath = new Stack<String>();
        converterPath.add("l");
        TypeConverterUtils.convertValue(outputRecordBuilder, converterPath, outputType, inputFormat);
        GenericRecord outputRecord = outputRecordBuilder.build();
        Assert.assertEquals(outputClass, outputRecord.get(0).getClass());
    }


    @Test
    public void testConvertValueToBoolean() {
        testConvertValue("false", TypeConverterProperties.TypeConverterOutputTypes.Boolean, null, Boolean.class);
    }

    @Test
    public void testConvertValueToDouble() {
        testConvertValue("2.5", TypeConverterProperties.TypeConverterOutputTypes.Double, null, Double.class);
    }

    @Test
    public void testConvertValueToFloat() {
        testConvertValue("3.5", TypeConverterProperties.TypeConverterOutputTypes.Float, null, Float.class);
    }

    @Test
    public void testConvertValueToInteger() {
        testConvertValue("1", TypeConverterProperties.TypeConverterOutputTypes.Integer, null, Integer.class);
    }

    @Test
    public void testConvertValueToLong() {
        testConvertValue("2", TypeConverterProperties.TypeConverterOutputTypes.Long, null, Long.class);
    }

    @Test
    public void testConvertValueToString() {
        testConvertValue(1, TypeConverterProperties.TypeConverterOutputTypes.String, null, String.class);
    }

    @Test
    public void testConvertValueToTimeNoFormat() {
        testConvertValue("10:20:15", TypeConverterProperties.TypeConverterOutputTypes.Time, null, Long.class);
    }

    @Test
    public void testConvertValueToTimeWithFormat() {
        testConvertValue("10:20:15", TypeConverterProperties.TypeConverterOutputTypes.Time, "ss:mm:HH", Long.class);
    }

    @Test
    public void testConvertValueToDateTimeNoFormat() {
        testConvertValue("2007-12-03T10:15:30", TypeConverterProperties.TypeConverterOutputTypes.DateTime, null, Long.class);
    }

    @Test
    public void testConvertValueToDateTimeWithFormat() {
        testConvertValue("03/12/2007 10:15:30", TypeConverterProperties.TypeConverterOutputTypes.DateTime, "dd/MM/yyyy HH:mm:ss", Long.class);
    }

    @Test
    public void testConvertValueToDateTimeWithFormat2() {
        testConvertValue("2016-10-17 15:21:23.135792 UTC", TypeConverterProperties.TypeConverterOutputTypes.DateTime, "yyyy-MM-dd HH:mm:ss.SSSSSS z", Long.class);
    }

    @Test
    public void testConvertValueToDecimalNoFormat() {
        testConvertValue("3.5", TypeConverterProperties.TypeConverterOutputTypes.Decimal, null, BigDecimal.class);
    }

    @Test
    public void testConvertValueToDecimalWithFormat() {
        testConvertValue("1,234", TypeConverterProperties.TypeConverterOutputTypes.Decimal, "#,###", BigDecimal.class);
    }

    @Test
    public void testGetPathSteps() {
        String pathSteps = ".a.b";
        Stack<String> result = TypeConverterUtils.getPathSteps(pathSteps);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("a", result.pop());
    }
}
