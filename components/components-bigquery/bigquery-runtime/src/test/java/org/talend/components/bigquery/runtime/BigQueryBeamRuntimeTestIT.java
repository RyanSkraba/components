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

package org.talend.components.bigquery.runtime;

import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatasetFromTable;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatastore;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createInput;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createOutput;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.TableRowJsonCoder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.BeamLocalRunnerOption;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.io.rowgenerator.RowGeneratorIO;
import org.talend.components.adapter.beam.utils.SparkRunnerTestUtils;
import org.talend.components.bigquery.BigQueryDatastoreProperties;
import org.talend.components.bigquery.input.BigQueryInputProperties;
import org.talend.components.bigquery.output.BigQueryOutputProperties;
import org.talend.daikon.avro.SampleSchemas;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;

public class BigQueryBeamRuntimeTestIT implements Serializable {
    @ClassRule
    public static final TestRule DISABLE_IF_NEEDED = new DisableIfMissingConfig("bigquery.project");

    final static String uuid = UUID.randomUUID().toString().replace("-", "_");

    final static String datasetName = "bqcomponentio" + uuid;

    BigQueryDatastoreProperties datastore;

    BeamJobRuntimeContainer runtimeContainer;

    SparkRunnerTestUtils sparkRunner;

    @BeforeClass
    public static void initDataset() {
        BigQuery bigquery = BigQueryConnection.createClient(createDatastore());
        DatasetId datasetId = DatasetId.of(BigQueryTestConstants.PROJECT, datasetName);
        bigquery.create(DatasetInfo.of(datasetId));
    }

    @AfterClass
    public static void cleanDataset() {
        BigQuery bigquery = BigQueryConnection.createClient(createDatastore());
        DatasetId datasetId = DatasetId.of(BigQueryTestConstants.PROJECT, datasetName);
        bigquery.delete(datasetId, BigQuery.DatasetDeleteOption.deleteContents());
    }

    @Before
    public void init() {
        datastore = createDatastore();
        runtimeContainer = new BeamJobRuntimeContainer(BeamLocalRunnerOption.getOptions());
    }

    @Test
    @Ignore("TestPipeline cause conflict issue, but when using direct runner, assert for bytes type is not working well")
    public void testAllTypesInputOutput_Local() throws UnsupportedEncodingException {
        testAllTypesInputOutput(Pipeline.create(BeamLocalRunnerOption.getOptions()));
    }

    @Test
    public void testAllTypesInputOutput_Spark() throws UnsupportedEncodingException {
        sparkRunner = new SparkRunnerTestUtils(this.getClass().getName());
        runtimeContainer = sparkRunner.createRuntimeContainer();
        testAllTypesInputOutput(sparkRunner.createPipeline());
    }

    /**
     *
     * Use direct runner instead of TestPipeline to avoid this issue, when upgrade to Beam 2.3.0
     *
     * Pipeline update will not be possible because the following transforms do not have stable unique names:
     * BigQueryOutputRuntime/BigQueryIO.Write/BatchLoads/TempFilePrefixView/Combine.GloballyAsSingletonView/Combine.globally(Singleton)/Combine.perKey(Singleton)/GroupByKey,
     * BigQueryOutputRuntime/BigQueryIO.Write/BatchLoads/View.AsSingleton/Combine.GloballyAsSingletonView/Combine.globally(Singleton)/Combine.perKey(Singleton)/GroupByKey.
     */
    @Test
    public void testPrimitivesRequiredInputOutput_Local() {
        testInputOutput(Pipeline.create(BeamLocalRunnerOption.getOptions()), "testPrimitiveRequiredLocal",
                SampleSchemas.recordPrimitivesRequired());
    }

    @Test
    public void testPrimitivesNullableInputOutput_Local() {
        testInputOutput(Pipeline.create(BeamLocalRunnerOption.getOptions()), "testPrimitiveNullableLocal",
                SampleSchemas.recordPrimitivesNullable());
    }

    @Test
    public void testCompositesRequired_Local() {
        testInputOutput(Pipeline.create(BeamLocalRunnerOption.getOptions()), "testCompositesRequiredLocal",
                SampleSchemas.recordCompositesRequired());
    }

    private void testAllTypesInputOutput(Pipeline pipeline) throws UnsupportedEncodingException {
        String tableName = "testalltypes";
        BigQueryOutputProperties outputProperties =
                createOutput(createDatasetFromTable(datastore, datasetName, tableName));
        outputProperties.tableOperation.setValue(BigQueryOutputProperties.TableOperation.DROP_IF_EXISTS_AND_CREATE);

        Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"BigQuerySchema\",\"fields\":[{\"name\":\"strCol\",\"type\":[\"string\",\"null\"]},{\"name\":\"bytesCol\",\"type\":[\"bytes\",\"null\"]},{\"name\":\"intCol\",\"type\":[\"long\",\"null\"]},{\"name\":\"floatCol\",\"type\":[\"double\",\"null\"]},{\"name\":\"boolCol\",\"type\":[\"boolean\",\"null\"]},{\"name\":\"timestampCol\",\"type\":[{\"type\":\"string\",\"talend.field.dbType\":\"TIMESTAMP\"},\"null\"]},{\"name\":\"dateCol\",\"type\":[{\"type\":\"string\",\"talend.field.dbType\":\"DATE\"},\"null\"]},{\"name\":\"timeCol\",\"type\":[{\"type\":\"string\",\"talend.field.dbType\":\"TIME\"},\"null\"]},{\"name\":\"datetimeCol\",\"type\":[{\"type\":\"string\",\"talend.field.dbType\":\"DATETIME\"},\"null\"]},{\"name\":\"strListCol\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"person\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"person\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"long\"},{\"name\":\"phones\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"addresses\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"addresses\",\"fields\":[{\"name\":\"city\",\"type\":\"string\"},{\"name\":\"address\",\"type\":\"string\"}]}}}]}}}]}");
        outputProperties.getDatasetProperties().main.schema.setValue(schema);

        BigQueryOutputRuntime outputRuntime = new BigQueryOutputRuntime();
        outputRuntime.initialize(runtimeContainer, outputProperties);

        List<TableRow> rows = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            rows.add(new TableRow() //
                    .set("strCol", "strValue" + i) //
                    .set("bytesCol", ("bytesCol" + i).getBytes("utf8")) //
                    .set("intCol", i) //
                    .set("floatCol", i * 9.9) //
                    .set("boolCol", i % 3 > 1) //
                    .set("timestampCol", "2016-10-17 15:21:23.135792 UTC") //
                    .set("dateCol", "2016-10-17") //
                    .set("timeCol", "15:21:23.123456") //
                    .set("datetimeCol", "2016-10-17T15:21:23.654321") //
                    .set("strListCol", Arrays.asList("a" + i, "b" + i, "c" + i)) //
                    .set("person", //
                            Arrays.asList( //
                                    new TableRow() //
                                            .set("name", "n1_" + i) //
                                            .set("age", i) //
                                            .set("phones", Arrays.asList("111" + i, "222" + i, "333" + i)) //
                                            .set("addresses", //
                                                    Arrays.asList( //
                                                            new TableRow() //
                                                                    .set("city", "Beijing") //
                                                                    .set("address", "who care" + i), //
                                                            new TableRow() //
                                                                    .set("city", "Tianjin") //
                                                                    .set("address", "don't know")) //
                                    ), //
                                    new TableRow() //
                                            .set("name", "n2_" + i) //
                                            .set("age", i) //
                                            .set("phones", Arrays.asList("111" + i, "222" + i, "333" + i)) //
                                            .set("addresses", //
                                                    Arrays.asList( //
                                                            new TableRow() //
                                                                    .set("city", "Beijing") //
                                                                    .set("address", "I care" + i), //
                                                            new TableRow() //
                                                                    .set("city", "Tianjin") //
                                                                    .set("address", "I know")) //
                                    ) //
                            ) //
            ) //
            );

        }

        pipeline
                .apply(Create.of(rows).withCoder(TableRowJsonCoder.of()))
                .apply(ParDo.of(new BigQueryInputRuntime.TableRowToIndexedRecordFn(schema)))
                .setCoder(LazyAvroCoder.of())
                .apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        // finish output
        // start input
        BigQueryInputProperties inputProperties =
                createInput(createDatasetFromTable(datastore, datasetName, tableName));

        BigQueryInputRuntime inputRuntime = new BigQueryInputRuntime();
        inputRuntime.initialize(runtimeContainer, inputProperties);

        PCollection<TableRow> tableRowPCollection = pipeline
                .apply(inputRuntime)
                .apply(ParDo.of(new BigQueryOutputRuntime.IndexedRecordToTableRowFn()))
                .setCoder(TableRowJsonCoder.of());

        PAssert.that(tableRowPCollection).containsInAnyOrder(rows);

        pipeline.run().waitUntilFinish();
    }

    private void testInputOutput(Pipeline pipeline, String tableName, Schema schema) {
        // Write 10 rows of generated records to BigQuery.
        {
            BigQueryOutputProperties outputProperties =
                    createOutput(createDatasetFromTable(datastore, datasetName, tableName));
            outputProperties.tableOperation.setValue(BigQueryOutputProperties.TableOperation.DROP_IF_EXISTS_AND_CREATE);
            outputProperties.getDatasetProperties().main.schema.setValue(schema);
            BigQueryOutputRuntime outputRuntime = new BigQueryOutputRuntime();
            outputRuntime.initialize(runtimeContainer, outputProperties);

            // Generate 10 rows.
            PCollection<IndexedRecord> pc1 =
                    pipeline.apply(RowGeneratorIO.read().withSchema(schema).withRows(10).withSeed(0L));

            pc1.apply(outputRuntime);
            pipeline.run().waitUntilFinish();
        }

        // Read the 10 rows back from BigQuery.
        {
            BigQueryInputProperties inputProperties =
                    createInput(createDatasetFromTable(datastore, datasetName, tableName));
            BigQueryInputRuntime inputRuntime = new BigQueryInputRuntime();
            inputRuntime.initialize(runtimeContainer, inputProperties);
            PCollection<IndexedRecord> pc2 = pipeline.apply(inputRuntime);
            // TODO: This only tests that 10 records were read back, we should probably check the values.
            // TODO: To be completed when RowGeneratorIO is more stable.
            // PAssert.that(pc2).containsInAnyOrder(...);
            PAssert.thatSingleton(pc2.apply("Count", Count.<IndexedRecord> globally())).isEqualTo(10L);
            pipeline.run().waitUntilFinish();
        }
    }
}
