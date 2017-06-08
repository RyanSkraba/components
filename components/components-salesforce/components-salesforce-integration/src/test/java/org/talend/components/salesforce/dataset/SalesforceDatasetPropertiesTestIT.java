package org.talend.components.salesforce.dataset;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties.SourceType;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.components.salesforce.integration.SalesforceTestBase;

public class SalesforceDatasetPropertiesTestIT extends SalesforceTestBase {

    @Test
    public void testAfterSourceType() {
        SalesforceDatasetProperties dataset = new SalesforceDatasetProperties("dataset");
        dataset.init();

        SalesforceDatastoreProperties datastore = new SalesforceDatastoreProperties("datastore");
        datastore.init();

        dataset.sourceType.setValue(SourceType.SOQL_QUERY);
        dataset.setDatastoreProperties(datastore);

        datastore.userId.setValue(userId);
        datastore.password.setValue(password);
        datastore.securityKey.setValue(securityKey);

        dataset.sourceType.setValue(SourceType.MODULE_SELECTION);

        List modules = dataset.moduleName.getPossibleValues();
        Assert.assertTrue("the module list is not empty before calling 'afterSourceType' method, not right",
                modules == null || modules.isEmpty());

        try {
            dataset.afterSourceType();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        modules = dataset.moduleName.getPossibleValues();
        Assert.assertTrue("the module list is empty after calling 'afterSourceType' method, not right",
                modules != null && !modules.isEmpty());
        Assert.assertTrue("the module value must be empty at this point", StringUtils.isEmpty(dataset.moduleName.getValue()));
        Assert.assertTrue("the list of selected columns or fields must be null or empty",
                dataset.selectColumnIds.getValue() == null || dataset.selectColumnIds.getValue().isEmpty());
        Assert.assertTrue("the query value must be empty", StringUtils.isEmpty(dataset.query.getValue()));
    }

    @Test
    public void testAfterModuleName() {
        SalesforceDatasetProperties dataset = new SalesforceDatasetProperties("dataset");
        dataset.init();

        SalesforceDatastoreProperties datastore = new SalesforceDatastoreProperties("datastore");
        datastore.init();

        dataset.sourceType.setValue(SourceType.SOQL_QUERY);
        dataset.setDatastoreProperties(datastore);

        datastore.userId.setValue(userId);
        datastore.password.setValue(password);
        datastore.securityKey.setValue(securityKey);

        dataset.sourceType.setValue(SourceType.MODULE_SELECTION);
        dataset.moduleName.setValue("Account");

        List modules = dataset.selectColumnIds.getPossibleValues();
        Assert.assertTrue("the module list is not empty before calling 'afterSourceType' method, not right",
                modules == null || modules.isEmpty());

        try {
            dataset.afterModuleName();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        modules = dataset.selectColumnIds.getPossibleValues();
        Assert.assertTrue("the module list is empty after calling 'afterSourceType' method, not right",
                modules != null && !modules.isEmpty());
        Assert.assertTrue("the list of selected columns or fields must be null or empty",
                dataset.selectColumnIds.getValue() == null || dataset.selectColumnIds.getValue().isEmpty());
        Assert.assertTrue("the query value must be empty", StringUtils.isEmpty(dataset.query.getValue()));

    }

}
