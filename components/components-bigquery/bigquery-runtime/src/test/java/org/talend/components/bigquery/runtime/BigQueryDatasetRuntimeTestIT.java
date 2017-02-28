package org.talend.components.bigquery.runtime;

import static org.junit.Assert.assertTrue;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatasetFromTable;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatastore;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.services.bigquery.BigqueryScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;

public class BigQueryDatasetRuntimeTestIT {

    BigQueryDatasetRuntime runtime;

    final static List<String> datasets = Arrays.asList("bqcomponentds1", "bqcomponentds2", "bqcomponentds3");

    final static List<String> tables = Arrays.asList("tb1", "tb2", "tb3");

    @BeforeClass
    public static void initDatasetAndTable() throws IOException {
        BigQuery bigquery = BigQueryConnection.createClient(createDatastore());
        for (String dataset : datasets) {
            DatasetId datasetId = DatasetId.of(BigQueryTestConstants.PROJECT, dataset);
            bigquery.delete(datasetId, BigQuery.DatasetDeleteOption.deleteContents());
            bigquery.create(DatasetInfo.of(datasetId));
        }

        for (String table : tables) {
            TableDefinition tableDefinition = StandardTableDefinition.of(Schema.of(Field.of("test", Field.Type.string())));
            TableId tableId = TableId.of(BigQueryTestConstants.PROJECT, datasets.get(0), table);
            bigquery.create(TableInfo.of(tableId, tableDefinition));
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

}
