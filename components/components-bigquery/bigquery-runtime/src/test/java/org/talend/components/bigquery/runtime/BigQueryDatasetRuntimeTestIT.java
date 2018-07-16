package org.talend.components.bigquery.runtime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatasetFromQuery;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatasetFromTable;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.daikon.java8.Consumer;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;

public class BigQueryDatasetRuntimeTestIT {

    final static String uuid = UUID.randomUUID().toString().replace("-", "_");

    final static List<String> datasets =
            Arrays.asList("bqcomponentds1" + uuid, "bqcomponentds2" + uuid, "bqcomponentds3" + uuid);

    final static List<String> tables = Arrays.asList("tb1", "tb2", "tb3");

    BigQueryDatasetRuntime runtime;

    @BeforeClass
    public static void initDatasetAndTable() throws IOException {
        BigQuery bigquery = BigQueryConnection.createClient(createDatastore());
        for (String dataset : datasets) {
            DatasetId datasetId = DatasetId.of(BigQueryTestConstants.PROJECT, dataset);
            bigquery.create(DatasetInfo.of(datasetId));
        }

        for (String table : tables) {
            TableDefinition tableDefinition =
                    StandardTableDefinition.of(Schema.of(Field.of("test", Field.Type.string())));
            TableId tableId = TableId.of(BigQueryTestConstants.PROJECT, datasets.get(0), table);
            bigquery.create(TableInfo.of(tableId, tableDefinition));
        }
    }

    @AfterClass
    public static void cleanDatasetAndTable() {
        BigQuery bigquery = BigQueryConnection.createClient(createDatastore());
        for (String dataset : datasets) {
            DatasetId datasetId = DatasetId.of(BigQueryTestConstants.PROJECT, dataset);
            bigquery.delete(datasetId, BigQuery.DatasetDeleteOption.deleteContents());
        }
    }

    @Before
    public void reset() throws IOException {
        runtime = new BigQueryDatasetRuntime();
    }

    @Test
    public void listDatasets() throws IOException {
        runtime.initialize(null, createDatasetFromTable(createDatastore(), "", ""));
        Set<String> retrievedDatasets = runtime.listDatasets();
        for (String dataset : datasets) {
            assertTrue(retrievedDatasets.contains(dataset));
        }
    }

    @Test
    public void listTables() throws IOException {
        runtime.initialize(null, createDatasetFromTable(createDatastore(), datasets.get(0), ""));
        Set<String> retrievedTables = runtime.listTables();
        for (String table : tables) {
            assertTrue(retrievedTables.contains(table));
        }
    }

    @Test
    @Ignore("Manual test as it depends on dev dataset")
    public void getBigSample1() {
        runtime.initialize(null, createDatasetFromTable(createDatastore(), "datastreams_celia_us", "stackoverflow"));
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual.size(), is(100));
    }

    @Test
    @Ignore("Manual test as it depends on dev dataset")
    public void getBigSample2() {
        runtime.initialize(null, createDatasetFromQuery(createDatastore(),
                "SELECT * FROM `engineering-152721.datastreams_celia_us.stackoverflow`", false));
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual.size(), is(100));
    }

    @Test
    @Ignore("Manual test as it depends on dev dataset")
    public void getBigSample3() {
        runtime.initialize(null, createDatasetFromQuery(createDatastore(),
                "SELECT * FROM [engineering-152721:datastreams_celia_us.stackoverflow]", true));
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual.size(), is(100));
    }

    @Test
    @Ignore("Manual test as it depends on dev dataset")
    public void getSample1() {
        runtime.initialize(null, createDatasetFromTable(createDatastore(), "datastreams_bchen", "testalltypes"));

        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual.toString(), is(
                "[{\"strCol\": \"strValue3\", \"bytesCol\": {\"bytes\": \"bytesCol3\"}, \"intCol\": 3, \"floatCol\": 29.700000000000003, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a3\", \"b3\", \"c3\"], \"person\": [{\"name\": \"n1_3\", \"age\": 3, \"phones\": [\"1113\", \"2223\", \"3333\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care3\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_3\", \"age\": 3, \"phones\": [\"1113\", \"2223\", \"3333\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care3\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue1\", \"bytesCol\": {\"bytes\": \"bytesCol1\"}, \"intCol\": 1, \"floatCol\": 9.9, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a1\", \"b1\", \"c1\"], \"person\": [{\"name\": \"n1_1\", \"age\": 1, \"phones\": [\"1111\", \"2221\", \"3331\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care1\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_1\", \"age\": 1, \"phones\": [\"1111\", \"2221\", \"3331\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care1\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue4\", \"bytesCol\": {\"bytes\": \"bytesCol4\"}, \"intCol\": 4, \"floatCol\": 39.6, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a4\", \"b4\", \"c4\"], \"person\": [{\"name\": \"n1_4\", \"age\": 4, \"phones\": [\"1114\", \"2224\", \"3334\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care4\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_4\", \"age\": 4, \"phones\": [\"1114\", \"2224\", \"3334\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care4\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue5\", \"bytesCol\": {\"bytes\": \"bytesCol5\"}, \"intCol\": 5, \"floatCol\": 49.5, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a5\", \"b5\", \"c5\"], \"person\": [{\"name\": \"n1_5\", \"age\": 5, \"phones\": [\"1115\", \"2225\", \"3335\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care5\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_5\", \"age\": 5, \"phones\": [\"1115\", \"2225\", \"3335\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care5\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue2\", \"bytesCol\": {\"bytes\": \"bytesCol2\"}, \"intCol\": 2, \"floatCol\": 19.8, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a2\", \"b2\", \"c2\"], \"person\": [{\"name\": \"n1_2\", \"age\": 2, \"phones\": [\"1112\", \"2222\", \"3332\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care2\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_2\", \"age\": 2, \"phones\": [\"1112\", \"2222\", \"3332\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care2\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}]"));
        assertThat(actual.size(), is(5));
    }

    @Test
    @Ignore("Manual test as it depends on dev dataset")
    public void getSample2() {
        runtime.initialize(null, createDatasetFromQuery(createDatastore(),
                "SELECT * FROM datastreams_bchen.testalltypes LIMIT 1000", false));

        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual.toString(), is(
                "[{\"strCol\": \"strValue3\", \"bytesCol\": {\"bytes\": \"bytesCol3\"}, \"intCol\": 3, \"floatCol\": 29.700000000000003, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a3\", \"b3\", \"c3\"], \"person\": [{\"name\": \"n1_3\", \"age\": 3, \"phones\": [\"1113\", \"2223\", \"3333\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care3\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_3\", \"age\": 3, \"phones\": [\"1113\", \"2223\", \"3333\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care3\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue1\", \"bytesCol\": {\"bytes\": \"bytesCol1\"}, \"intCol\": 1, \"floatCol\": 9.9, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a1\", \"b1\", \"c1\"], \"person\": [{\"name\": \"n1_1\", \"age\": 1, \"phones\": [\"1111\", \"2221\", \"3331\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care1\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_1\", \"age\": 1, \"phones\": [\"1111\", \"2221\", \"3331\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care1\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue4\", \"bytesCol\": {\"bytes\": \"bytesCol4\"}, \"intCol\": 4, \"floatCol\": 39.6, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a4\", \"b4\", \"c4\"], \"person\": [{\"name\": \"n1_4\", \"age\": 4, \"phones\": [\"1114\", \"2224\", \"3334\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care4\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_4\", \"age\": 4, \"phones\": [\"1114\", \"2224\", \"3334\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care4\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue5\", \"bytesCol\": {\"bytes\": \"bytesCol5\"}, \"intCol\": 5, \"floatCol\": 49.5, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a5\", \"b5\", \"c5\"], \"person\": [{\"name\": \"n1_5\", \"age\": 5, \"phones\": [\"1115\", \"2225\", \"3335\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care5\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_5\", \"age\": 5, \"phones\": [\"1115\", \"2225\", \"3335\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care5\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}, {\"strCol\": \"strValue2\", \"bytesCol\": {\"bytes\": \"bytesCol2\"}, \"intCol\": 2, \"floatCol\": 19.8, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"strListCol\": [\"a2\", \"b2\", \"c2\"], \"person\": [{\"name\": \"n1_2\", \"age\": 2, \"phones\": [\"1112\", \"2222\", \"3332\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"who care2\"}, {\"city\": \"Tianjin\", \"address\": \"don't know\"}]}, {\"name\": \"n2_2\", \"age\": 2, \"phones\": [\"1112\", \"2222\", \"3332\"], \"addresses\": [{\"city\": \"Beijing\", \"address\": \"I care2\"}, {\"city\": \"Tianjin\", \"address\": \"I know\"}]}]}]"));
        assertThat(actual.size(), is(5));
    }

    @Test
    @Ignore("Manual test as it depends on dev dataset")
    public void getSample3() {
        // runtime.initialize(null, createDatasetFromQuery(createDatastore(), "SELECT * FROM
        // [datastreams_bchen.testalltypes_new] LIMIT 1000", true)); //wrong sql even on bigquery console, Cannot output
        // multiple independently repeated fields at the same time

        runtime.initialize(null, createDatasetFromQuery(createDatastore(),
                "SELECT strCol, floatCol, bytesCol, intCol,boolCol, timestampCol, dateCol, timeCol, datetimeCol, person.name, person.age, person.addresses.city,person.addresses.address FROM [datastreams_bchen.testalltypes_new] LIMIT 1000",
                true));
        // but flatten result
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual.toString(), is(
                "[{\"strCol\": \"strValue4\", \"floatCol\": 39.6, \"bytesCol\": {\"bytes\": \"bytesCol4\"}, \"intCol\": 4, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_4\", \"person_age\": 4, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"who care4\"}, {\"strCol\": \"strValue4\", \"floatCol\": 39.6, \"bytesCol\": {\"bytes\": \"bytesCol4\"}, \"intCol\": 4, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_4\", \"person_age\": 4, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"don't know\"}, {\"strCol\": \"strValue4\", \"floatCol\": 39.6, \"bytesCol\": {\"bytes\": \"bytesCol4\"}, \"intCol\": 4, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_4\", \"person_age\": 4, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"I care4\"}, {\"strCol\": \"strValue4\", \"floatCol\": 39.6, \"bytesCol\": {\"bytes\": \"bytesCol4\"}, \"intCol\": 4, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_4\", \"person_age\": 4, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"I know\"}, {\"strCol\": \"strValue1\", \"floatCol\": 9.9, \"bytesCol\": {\"bytes\": \"bytesCol1\"}, \"intCol\": 1, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_1\", \"person_age\": 1, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"who care1\"}, {\"strCol\": \"strValue1\", \"floatCol\": 9.9, \"bytesCol\": {\"bytes\": \"bytesCol1\"}, \"intCol\": 1, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_1\", \"person_age\": 1, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"don't know\"}, {\"strCol\": \"strValue1\", \"floatCol\": 9.9, \"bytesCol\": {\"bytes\": \"bytesCol1\"}, \"intCol\": 1, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_1\", \"person_age\": 1, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"I care1\"}, {\"strCol\": \"strValue1\", \"floatCol\": 9.9, \"bytesCol\": {\"bytes\": \"bytesCol1\"}, \"intCol\": 1, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_1\", \"person_age\": 1, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"I know\"}, {\"strCol\": \"strValue3\", \"floatCol\": 29.700000000000003, \"bytesCol\": {\"bytes\": \"bytesCol3\"}, \"intCol\": 3, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_3\", \"person_age\": 3, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"who care3\"}, {\"strCol\": \"strValue3\", \"floatCol\": 29.700000000000003, \"bytesCol\": {\"bytes\": \"bytesCol3\"}, \"intCol\": 3, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_3\", \"person_age\": 3, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"don't know\"}, {\"strCol\": \"strValue3\", \"floatCol\": 29.700000000000003, \"bytesCol\": {\"bytes\": \"bytesCol3\"}, \"intCol\": 3, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_3\", \"person_age\": 3, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"I care3\"}, {\"strCol\": \"strValue3\", \"floatCol\": 29.700000000000003, \"bytesCol\": {\"bytes\": \"bytesCol3\"}, \"intCol\": 3, \"boolCol\": false, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_3\", \"person_age\": 3, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"I know\"}, {\"strCol\": \"strValue5\", \"floatCol\": 49.5, \"bytesCol\": {\"bytes\": \"bytesCol5\"}, \"intCol\": 5, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_5\", \"person_age\": 5, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"who care5\"}, {\"strCol\": \"strValue5\", \"floatCol\": 49.5, \"bytesCol\": {\"bytes\": \"bytesCol5\"}, \"intCol\": 5, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_5\", \"person_age\": 5, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"don't know\"}, {\"strCol\": \"strValue5\", \"floatCol\": 49.5, \"bytesCol\": {\"bytes\": \"bytesCol5\"}, \"intCol\": 5, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_5\", \"person_age\": 5, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"I care5\"}, {\"strCol\": \"strValue5\", \"floatCol\": 49.5, \"bytesCol\": {\"bytes\": \"bytesCol5\"}, \"intCol\": 5, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_5\", \"person_age\": 5, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"I know\"}, {\"strCol\": \"strValue2\", \"floatCol\": 19.8, \"bytesCol\": {\"bytes\": \"bytesCol2\"}, \"intCol\": 2, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_2\", \"person_age\": 2, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"who care2\"}, {\"strCol\": \"strValue2\", \"floatCol\": 19.8, \"bytesCol\": {\"bytes\": \"bytesCol2\"}, \"intCol\": 2, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n1_2\", \"person_age\": 2, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"don't know\"}, {\"strCol\": \"strValue2\", \"floatCol\": 19.8, \"bytesCol\": {\"bytes\": \"bytesCol2\"}, \"intCol\": 2, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_2\", \"person_age\": 2, \"person_addresses_city\": \"Beijing\", \"person_addresses_address\": \"I care2\"}, {\"strCol\": \"strValue2\", \"floatCol\": 19.8, \"bytesCol\": {\"bytes\": \"bytesCol2\"}, \"intCol\": 2, \"boolCol\": true, \"timestampCol\": \"2016-10-17 15:21:23.135792 UTC\", \"dateCol\": \"2016-10-17\", \"timeCol\": \"15:21:23.123456\", \"datetimeCol\": \"2016-10-17T15:21:23.654321\", \"person_name\": \"n2_2\", \"person_age\": 2, \"person_addresses_city\": \"Tianjin\", \"person_addresses_address\": \"I know\"}]"));
        assertThat(actual.size(), is(20));
    }

}
