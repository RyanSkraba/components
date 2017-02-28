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

import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.components.bigquery.BigQueryDatastoreProperties;
import org.talend.components.bigquery.input.BigQueryInputProperties;
import org.talend.components.bigquery.output.BigQueryOutputProperties;

public class BigQueryTestConstants {

    public static final String PROJECT;

    public static final String SERVICE_ACCOUNT_FILE;

    public static final String GCP_TEMP_FOLDER;

    static {
        PROJECT = System.getProperty("bigquery.project");
        SERVICE_ACCOUNT_FILE = System.getProperty("bigquery.service.account.file");
        GCP_TEMP_FOLDER = System.getProperty("bigquery.gcp.temp.folder");
    }

    public static BigQueryDatastoreProperties createDatastore() {
        BigQueryDatastoreProperties datastore = new BigQueryDatastoreProperties("datastore");
        datastore.init();
        datastore.projectName.setValue(BigQueryTestConstants.PROJECT);
        datastore.serviceAccountFile.setValue(BigQueryTestConstants.SERVICE_ACCOUNT_FILE);
        datastore.tempGsFolder.setValue(BigQueryTestConstants.GCP_TEMP_FOLDER);
        return datastore;
    }

    public static BigQueryDatasetProperties createDatasetFromTable(BigQueryDatastoreProperties datastore, String datasetName,
            String tableName) {
        BigQueryDatasetProperties dataset = new BigQueryDatasetProperties("dataset");
        dataset.init();
        dataset.setDatastoreProperties(datastore);
        dataset.bqDataset.setValue(datasetName);
        dataset.sourceType.setValue(BigQueryDatasetProperties.SourceType.TABLE_NAME);
        dataset.tableName.setValue(tableName);
        return dataset;
    }

    public static BigQueryDatasetProperties createDatasetFromQuery(BigQueryDatastoreProperties datastore, String query,
            boolean useLegacy) {
        BigQueryDatasetProperties dataset = new BigQueryDatasetProperties("dataset");
        dataset.init();
        dataset.setDatastoreProperties(datastore);
        dataset.sourceType.setValue(BigQueryDatasetProperties.SourceType.QUERY);
        dataset.query.setValue(query);
        dataset.useLegacySql.setValue(useLegacy);
        return dataset;
    }

    public static BigQueryInputProperties createInput(BigQueryDatasetProperties dataset) {
        BigQueryInputProperties input = new BigQueryInputProperties("input");
        input.init();
        input.setDatasetProperties(dataset);
        return input;
    }

    public static BigQueryOutputProperties createOutput(BigQueryDatasetProperties dataset) {
        BigQueryOutputProperties output = new BigQueryOutputProperties("output");
        output.init();
        output.setDatasetProperties(dataset);
        return output;
    }
}
