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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.talend.components.salesforce.SalesforceRuntimeTestUtil;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;

/**
 * Created by wwang on 2016-05-16.
 */
public class SalesforceBulkLoadTestIT extends SalesforceTestBase {

	private SalesforceRuntimeTestUtil util = new SalesforceRuntimeTestUtil();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Ignore
	@Test
	public void testUpdate() throws Throwable {
		util.createTestData();

	}

	@Test
	public void testInsert() throws Throwable {
		String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

		// outputbulk part
		TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
				.getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);
		TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file);
		util.simulateRuntimeCaller(definition, modelProperties);

		//bulkexec part
		TSalesforceBulkExecDefinition defin = (TSalesforceBulkExecDefinition) getComponentService()
				.getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
		util.simulateRuntimeCaller(defin, data_file);
	}

	@Ignore
	@Test
	public void testDelete() throws Throwable {
		util.createTestData();

	}

}
