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
package org.talend.components.salesforce.runtime.dataprep;

import java.util.Arrays;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

/**
 * the data store runtime for salesforce
 *
 */
public class SalesforceDatastoreRuntime implements DatastoreRuntime<SalesforceDatastoreProperties> {

    protected SalesforceDatastoreProperties datastore;

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        SalesforceDataprepSource sds = new SalesforceDataprepSource();

        SalesforceInputProperties properties = new SalesforceInputProperties("model");
        SalesforceDatasetProperties dataset = new SalesforceDatasetProperties("dataset");
        //set the sourcetype to soql to prevent loading of all modules during health check
        dataset.sourceType.setValue(SalesforceDatasetProperties.SourceType.SOQL_QUERY);
        properties.setDatasetProperties(dataset);
        dataset.setDatastoreProperties(datastore);

        sds.initialize(container, properties);
        ValidationResult result = sds.validate(container);
        return Arrays.asList(result);
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, SalesforceDatastoreProperties properties) {
        this.datastore = properties;
        return ValidationResult.OK;
    }
}
