// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.talend.components.salesforce.SalesforceRuntimeTestUtil;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;

/**
 * Created by wwang on 2016-03-09.
 */
public class SalesforceBulkFileWriterTestIT extends SalesforceTestBase {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    SalesforceRuntimeTestUtil util = new SalesforceRuntimeTestUtil();

    @Test
    public void testBasic() throws Exception {
        TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
                .getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);

        String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

        TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file,
                util.getTestSchema1());

        String[] expected = { "FirstName", "LastName", "Phone" };
        List actual = modelProperties.upsertRelationTable.columnName.getPossibleValues();
        Assert.assertArrayEquals(expected, actual.toArray());

        util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema1(), util.getTestData());

        String[] expected1 = { "FirstName,LastName,Phone", "Wei,Wang,010-11111111", "Jin,Zhao,010-11111112", "Wei,Yuan,#N/A" };
        util.compareFileContent(data_file, expected1);

        // test ignore null
        modelProperties.ignoreNull.setValue("true");
        util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema1(), util.getTestData());

        String[] expected2 = { "FirstName,LastName,Phone", "Wei,Wang,010-11111111", "Jin,Zhao,010-11111112", "Wei,Yuan," };
        util.compareFileContent(data_file, expected2);

        // test append
        modelProperties.append.setValue("true");
        util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema1(), util.getTestData());

        String[] expected3 = { "FirstName,LastName,Phone", "Wei,Wang,010-11111111", "Jin,Zhao,010-11111112", "Wei,Yuan,",
                "Wei,Wang,010-11111111", "Jin,Zhao,010-11111112", "Wei,Yuan," };
        util.compareFileContent(data_file, expected3);

    }

    @Test
    public void testAppendWhenFileIsEmptyOrNotExist() throws Exception {
        TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
                .getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);

        String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

        TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file,
                util.getTestSchema1());

        modelProperties.append.setValue("true");

        util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema1(), util.getTestData());

        String[] expected1 = { "FirstName,LastName,Phone", "Wei,Wang,010-11111111", "Jin,Zhao,010-11111112", "Wei,Yuan,#N/A" };
        util.compareFileContent(data_file, expected1);
    }

    @Test
    public void testUpsertAction() throws Exception {
        TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
                .getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);

        String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

        TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file,
                util.getTestSchema1());

        java.util.List<String> columnNames = new java.util.ArrayList<String>();
        columnNames.add("Phone");
        modelProperties.upsertRelationTable.columnName.setValue(columnNames);

        java.util.List<String> lookupFieldExternalIdNames = new java.util.ArrayList<String>();
        lookupFieldExternalIdNames.add("eid");
        modelProperties.upsertRelationTable.lookupFieldExternalIdName.setValue(lookupFieldExternalIdNames);

        java.util.List<String> lookupFieldNames = new java.util.ArrayList<String>();
        lookupFieldNames.add("lfn");
        modelProperties.upsertRelationTable.lookupFieldName.setValue(lookupFieldNames);

        java.util.List<String> polymorphics = new java.util.ArrayList<String>();
        polymorphics.add("true");
        modelProperties.upsertRelationTable.polymorphic.setValue(polymorphics);

        java.util.List<String> lookupFieldModuleNames = new java.util.ArrayList<String>();
        lookupFieldModuleNames.add("fmn");
        modelProperties.upsertRelationTable.lookupFieldModuleName.setValue(lookupFieldModuleNames);

        util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema1(), util.getTestData());

        String[] expected1 = { "FirstName,LastName,fmn:lfn.eid", "Wei,Wang,010-11111111", "Jin,Zhao,010-11111112",
                "Wei,Yuan,#N/A" };
        util.compareFileContent(data_file, expected1);
    }

}
