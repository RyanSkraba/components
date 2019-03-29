package org.talend.components.processing.runtime.aggregate;

import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.utils.SparkRunnerTestUtils;
import org.talend.components.processing.definition.aggregate.AggregateFieldOperationType;
import org.talend.components.processing.definition.aggregate.AggregateGroupByProperties;
import org.talend.components.processing.definition.aggregate.AggregateOperationProperties;
import org.talend.components.processing.definition.aggregate.AggregateProperties;

public class AggregateRuntimeTest {

    private final Schema basicSchema = SchemaBuilder
            .record("basic")
            .fields()
            .name("g1")
            .type()
            .stringType()
            .noDefault()
            .name("g2")
            .type()
            .stringType()
            .noDefault()
            .name("int1")
            .type()
            .unionOf()
            .intType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("long1")
            .type()
            .unionOf()
            .longType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("float1")
            .type()
            .unionOf()
            .floatType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("double1")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("array1")
            .type()
            .unionOf()
            .array()
            .items()
            .stringType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .endRecord();

    private final IndexedRecord basic1 = new GenericRecordBuilder(basicSchema)
            .set("g1", "teamA")
            .set("g2", "sub1")
            .set("int1", 1)
            .set("long1", 1l)
            .set("float1", 1.0f)
            .set("double1", 1.0)
            .set("array1", Arrays.asList("a", "a", "a"))
            .build();

    private final IndexedRecord basic2 = new GenericRecordBuilder(basicSchema)
            .set("g1", "teamA")
            .set("g2", "sub1")
            .set("int1", 2)
            .set("long1", 2l)
            .set("float1", 2.0f)
            .set("double1", 2.0)
            .set("array1", Arrays.asList("a", "a", "a"))
            .build();

    private final IndexedRecord basic3 = new GenericRecordBuilder(basicSchema)
            .set("g1", "teamA")
            .set("g2", "sub1")
            .set("int1", 3)
            .set("long1", 3l)
            .set("float1", 3.0f)
            .set("double1", 3.0)
            .set("array1", Arrays.asList("a", "a", "a"))
            .build();

    private final IndexedRecord basic4 = new GenericRecordBuilder(basicSchema)
            .set("g1", "teamA")
            .set("g2", "sub2")
            .set("int1", 4)
            .set("long1", 4l)
            .set("float1", 4.0f)
            .set("double1", 4.0)
            .set("array1", Arrays.asList("b", "b"))
            .build();

    private final IndexedRecord basic5 = new GenericRecordBuilder(basicSchema)
            .set("g1", "teamA")
            .set("g2", "sub2")
            .set("int1", 5)
            .set("long1", 5l)
            .set("float1", 5.0f)
            .set("double1", 5.0)
            .set("array1", Arrays.asList("b", "b"))
            .build();

    private final IndexedRecord basic6 = new GenericRecordBuilder(basicSchema)
            .set("g1", "teamB")
            .set("g2", "sub1")
            .set("int1", 6)
            .set("long1", 6l)
            .set("float1", 6.0f)
            .set("double1", 6.0)
            .set("array1", Arrays.asList("c"))
            .build();

    private final IndexedRecord basic7 = new GenericRecordBuilder(basicSchema)
            .set("g1", "teamC")
            .set("g2", "sub1")
            .set("int1", null)
            .set("long1", null)
            .set("float1", null)
            .set("double1", null)
            .set("array1", null)
            .build();

    private final List<IndexedRecord> basicList = Arrays.asList(basic1, basic2, basic3, basic4, basic5, basic6, basic7);

    private final Schema basicResultSchema = SchemaBuilder
            .record("basicResult")
            .fields()
            .name("g1")
            .type()
            .stringType()
            .noDefault()
            .name("g2")
            .type()
            .stringType()
            .noDefault()

            .name("int1_MIN")
            .type()
            .unionOf()
            .intType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("long1_MIN")
            .type()
            .unionOf()
            .longType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("float1_MIN")
            .type()
            .unionOf()
            .floatType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("double1_MIN")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .name("int1_MAX")
            .type()
            .unionOf()
            .intType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("long1_MAX")
            .type()
            .unionOf()
            .longType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("float1_MAX")
            .type()
            .unionOf()
            .floatType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("double1_MAX")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .name("int1_AVG")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("long1_AVG")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("float1_AVG")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("double1_AVG")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .name("int1_SUM")
            .type()
            .unionOf()
            .longType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("long1_SUM")
            .type()
            .unionOf()
            .longType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("float1_SUM")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()
            .name("double1_SUM")
            .type()
            .unionOf()
            .doubleType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .name("array1_LIST")
            .type()
            .unionOf()
            .array()
            .items()
            .unionOf()
            .array()
            .items()
            .stringType()
            .and()
            .nullType()
            .endUnion()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .name("array1_COUNT")
            .type()
            .unionOf()
            .longType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .name("g2_list_value")
            .type()
            .unionOf()
            .array()
            .items()
            .stringType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .name("g1_count_number")
            .type()
            .unionOf()
            .longType()
            .and()
            .nullType()
            .endUnion()
            .noDefault()

            .endRecord();

    private final IndexedRecord basicResult1 = new GenericRecordBuilder(basicResultSchema) //
            .set("g1", "teamA")
            .set("g2", "sub1")
            .set("int1_MIN", 1)
            .set("long1_MIN", 1l)
            .set("float1_MIN", 1.0f)
            .set("double1_MIN", 1.0)
            .set("int1_MAX", 3)
            .set("long1_MAX", 3l)
            .set("float1_MAX", 3.0f)
            .set("double1_MAX", 3.0)
            .set("int1_AVG", 2.0)
            .set("long1_AVG", 2.0)
            .set("float1_AVG", 2.0)
            .set("double1_AVG", 2.0)
            .set("int1_SUM", 6l)
            .set("long1_SUM", 6l)
            .set("float1_SUM", 6.0)
            .set("double1_SUM", 6.0)
            .set("array1_LIST",
                    Arrays.asList(Arrays.asList("a", "a", "a"), Arrays.asList("a", "a", "a"),
                            Arrays.asList("a", "a", "a")))
            .set("array1_COUNT", 3l)
            .set("g2_list_value", Arrays.asList("sub1", "sub1", "sub1"))
            .set("g1_count_number", 3l)
            .build();

    private final IndexedRecord basicResult2 = new GenericRecordBuilder(basicResultSchema) //
            .set("g1", "teamA")
            .set("g2", "sub2")
            .set("int1_MIN", 4)
            .set("long1_MIN", 4l)
            .set("float1_MIN", 4.0f)
            .set("double1_MIN", 4.0)
            .set("int1_MAX", 5)
            .set("long1_MAX", 5l)
            .set("float1_MAX", 5.0f)
            .set("double1_MAX", 5.0)
            .set("int1_AVG", 4.5)
            .set("long1_AVG", 4.5)
            .set("float1_AVG", 4.5)
            .set("double1_AVG", 4.5)
            .set("int1_SUM", 9l)
            .set("long1_SUM", 9l)
            .set("float1_SUM", 9.0)
            .set("double1_SUM", 9.0)
            .set("array1_LIST", Arrays.asList(Arrays.asList("b", "b"), Arrays.asList("b", "b")))
            .set("array1_COUNT", 2l)
            .set("g2_list_value", Arrays.asList("sub2", "sub2"))
            .set("g1_count_number", 2l)
            .build();

    private final IndexedRecord basicResult3 = new GenericRecordBuilder(basicResultSchema) //
            .set("g1", "teamB")
            .set("g2", "sub1")
            .set("int1_MIN", 6)
            .set("long1_MIN", 6l)
            .set("float1_MIN", 6.0f)
            .set("double1_MIN", 6.0)
            .set("int1_MAX", 6)
            .set("long1_MAX", 6l)
            .set("float1_MAX", 6.0f)
            .set("double1_MAX", 6.0)
            .set("int1_AVG", 6.0)
            .set("long1_AVG", 6.0)
            .set("float1_AVG", 6.0)
            .set("double1_AVG", 6.0)
            .set("int1_SUM", 6l)
            .set("long1_SUM", 6l)
            .set("float1_SUM", 6.0)
            .set("double1_SUM", 6.0)
            .set("array1_LIST", Arrays.asList(Arrays.asList("c")))
            .set("array1_COUNT", 1l)
            .set("g2_list_value", Arrays.asList("sub1"))
            .set("g1_count_number", 1l)
            .build();

    private static List nullArray = new ArrayList();
    static {
        nullArray.add(null);
    }

    private final IndexedRecord basicResult4 = new GenericRecordBuilder(basicResultSchema) //
            .set("g1", "teamC")
            .set("g2", "sub1")
            .set("int1_MIN", null)
            .set("long1_MIN", null)
            .set("float1_MIN", null)
            .set("double1_MIN", null)
            .set("int1_MAX", null)
            .set("long1_MAX", null)
            .set("float1_MAX", null)
            .set("double1_MAX", null)
            .set("int1_AVG", null)
            .set("long1_AVG", null)
            .set("float1_AVG", null)
            .set("double1_AVG", null)
            .set("int1_SUM", null)
            .set("long1_SUM", null)
            .set("float1_SUM", null)
            .set("double1_SUM", null)
            .set("array1_LIST", nullArray)
            .set("array1_COUNT", 1l)
            .set("g2_list_value", Arrays.asList("sub1"))
            .set("g1_count_number", 1l)
            .build();

    private final List<IndexedRecord> basicResultList =
            Arrays.asList(basicResult1, basicResult2, basicResult3, basicResult4);

    @Rule
    public TestPipeline pipeline = TestPipeline.create();

    private void addIntoGroup(AggregateProperties props, List<String> groupPaths) {
        for (String groupPath : groupPaths) {
            AggregateGroupByProperties groupProps = new AggregateGroupByProperties("group");
            groupProps.init();
            groupProps.fieldPath.setValue(groupPath);
            props.groupBy.addRow(groupProps);
        }
    }

    private void addIntoOperations(AggregateProperties props, AggregateFieldOperationType func,
            List<String> fieldPaths) {
        for (String fieldPath : fieldPaths) {
            AggregateOperationProperties operationProps = new AggregateOperationProperties("operation");
            operationProps.init();
            operationProps.fieldPath.setValue(fieldPath);
            operationProps.operation.setValue(func);
            props.operations.addRow(operationProps);
        }
    }

    private void addIntoOperations(AggregateProperties props, AggregateFieldOperationType func, String fieldPath,
            String outputFieldPath) {
        AggregateOperationProperties operationProps = new AggregateOperationProperties("operation");
        operationProps.init();
        operationProps.fieldPath.setValue(fieldPath);
        operationProps.operation.setValue(func);
        operationProps.outputFieldPath.setValue(outputFieldPath);
        props.operations.addRow(operationProps);
    }

    @Test
    public void basicTest_Local() {
        basicTest(pipeline);
    }

    @Test
    public void basicTest_Spark() {
        assumeTrue(System.getProperty("java.version").startsWith("1.8.")); // spark does not handle java 11 for now
        basicTest(new SparkRunnerTestUtils(this.getClass().getName()).createPipeline());
    }

    public void basicTest(Pipeline pipeline) {
        AggregateRuntime aggregateRuntime = new AggregateRuntime();
        AggregateProperties props = new AggregateProperties("aggregate");
        props.init();

        addIntoGroup(props, Arrays.asList(".g1", ".g2"));

        addIntoOperations(props, AggregateFieldOperationType.MIN,
                Arrays.asList(".int1", ".long1", ".float1", ".double1"));

        addIntoOperations(props, AggregateFieldOperationType.MAX,
                Arrays.asList(".int1", ".long1", ".float1", ".double1"));

        addIntoOperations(props, AggregateFieldOperationType.AVG,
                Arrays.asList(".int1", ".long1", ".float1", ".double1"));

        addIntoOperations(props, AggregateFieldOperationType.SUM,
                Arrays.asList(".int1", ".long1", ".float1", ".double1"));

        addIntoOperations(props, AggregateFieldOperationType.LIST, Arrays.asList(".array1"));

        addIntoOperations(props, AggregateFieldOperationType.COUNT, Arrays.asList(".array1"));

        addIntoOperations(props, AggregateFieldOperationType.LIST, ".g2", "g2_list_value");

        addIntoOperations(props, AggregateFieldOperationType.COUNT, ".g1", "g1_count_number");

        aggregateRuntime.initialize(null, props);

        PCollection<IndexedRecord> result = pipeline.apply(Create.of(basicList)).apply(aggregateRuntime);
        PAssert.that(result).containsInAnyOrder(basicResultList);

        pipeline.run();
    }

    /**
     * Tests the results when there are no group or operations. This is normally not an allowed state, except when the
     * component has been added to a pipeline without any configuration.
     */
    @Test
    public void basicTestEmptyForm() {
        AggregateRuntime aggregateRuntime = new AggregateRuntime();
        AggregateProperties props = new AggregateProperties("aggregate");
        props.init();

        addIntoGroup(props, Arrays.asList(""));

        // This is the part that can only happen when the component is in a default state.
        addIntoOperations(props, AggregateFieldOperationType.MIN, Arrays.asList(""));

        aggregateRuntime.initialize(null, props);

        PCollection<IndexedRecord> result = pipeline.apply(Create.of(basicList)).apply(aggregateRuntime);

        PAssert.that(result).containsInAnyOrder(new ArrayList<IndexedRecord>());

        pipeline.run();
    }
}
