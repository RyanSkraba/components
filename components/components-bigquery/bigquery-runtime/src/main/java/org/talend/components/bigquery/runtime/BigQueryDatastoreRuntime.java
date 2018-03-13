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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.bigquery.BigQueryDatastoreProperties;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.daikon.properties.ValidationResult;

import com.google.cloud.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;

public class BigQueryDatastoreRuntime implements DatastoreRuntime<BigQueryDatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private BigQueryDatastoreProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, BigQueryDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    /**
     * Check connection and authentication
     *
     * @param container
     * @return
     */
    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        BigQuery bigquery = BigQueryConnection.createClient(properties);
        bigquery.listDatasets(BigQuery.DatasetListOption.pageSize(1));
        return Arrays.asList(ValidationResult.OK);
    }
}
