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
package org.talend.components.salesforce.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.salesforce.test.SalesforceRuntimeTestUtil;
import org.talend.components.salesforce.test.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * Created by wwang on 2016-05-16.
 */
public class SalesforceBulkLoadTestIT extends SalesforceTestBase {

	private SalesforceRuntimeTestUtil util = new SalesforceRuntimeTestUtil();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testInsert() throws Throwable {
		String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

		// outputbulk part
		TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
				.getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);
		TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file,
				util.getTestSchema1());
		util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema1(), util.getTestData());

		// bulkexec part
		TSalesforceBulkExecDefinition defin = (TSalesforceBulkExecDefinition) getComponentService()
				.getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
		TSalesforceBulkExecProperties modelProps = (TSalesforceBulkExecProperties) defin.createRuntimeProperties();
		Reader reader = util.initReader(defin, data_file, modelProps, util.getTestSchema1(), util.getTestSchema2());

		modelProps.outputAction.setValue(TSalesforceBulkExecProperties.OutputAction.INSERT);

		List<String> ids = new ArrayList<String>();
		List<String> sids = new ArrayList<String>();
		try {
			IndexedRecordConverter<Object, ? extends IndexedRecord> factory = null;

			final List<Map<String, String>> result = new ArrayList<Map<String, String>>();

			for (boolean available = reader.start(); available; available = reader.advance()) {
				try {
					Object data = reader.getCurrent();

					factory = initCurrentData(factory, data);
					IndexedRecord record = factory.convertToAvro(data);

					String id = (String) record.get(0);
					ids.add(id);

					String firstname = (String) record.get(1);
					String lasttname = (String) record.get(2);
					String phone = (String) record.get(3);

					String salesforce_id = (String) record.get(4);
					sids.add(salesforce_id);

					Map<String, String> row = new HashMap<String, String>();
					row.put("FirstName", firstname);
					row.put("LastName", lasttname);
					row.put("Phone", phone);
					result.add(row);

				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			Assert.assertEquals(ids, sids);
			Assert.assertEquals(util.getTestData(), result);
		} finally {
			try {
				reader.close();
			} finally {
				util.deleteTestData(ids);
			}
		}
	}

	@Test
	public void testDelete() throws Throwable {
		List<String> ids = util.createTestData();

		final List<Map<String, String>> testData = new ArrayList<Map<String, String>>();
		for (String id : ids) {
			Map<String, String> row = new HashMap<String, String>();
			row.put("Id", id);
			testData.add(row);
		}

		String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

		// outputbulk part
		TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
				.getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);
		TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file,
				util.getTestSchema3());
		util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema3(), testData);

		// bulkexec part
		TSalesforceBulkExecDefinition defin = (TSalesforceBulkExecDefinition) getComponentService()
				.getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
		TSalesforceBulkExecProperties modelProps = (TSalesforceBulkExecProperties) defin.createRuntimeProperties();
		Reader reader = util.initReader(defin, data_file, modelProps, util.getTestSchema1(), util.getTestSchema3());

		modelProps.outputAction.setValue(TSalesforceBulkExecProperties.OutputAction.DELETE);

		try {
			IndexedRecordConverter<Object, ? extends IndexedRecord> factory = null;

			List<String> resultIds = new ArrayList<String>();
			for (boolean available = reader.start(); available; available = reader.advance()) {
				try {
					Object data = reader.getCurrent();

					factory = initCurrentData(factory, data);
					IndexedRecord record = factory.convertToAvro(data);

					String id = (String) record.get(0);
					resultIds.add(id);
				} catch (Exception e) {
					Assert.fail(e.getMessage());
				}
			}

			Assert.assertEquals(ids, resultIds);
		} finally {
			try {
				reader.close();
			} finally {
				// do nothing
			}
		}
	}

	@Test
	public void testUpdate() throws Throwable {
		List<String> ids = util.createTestData();

		String id = ids.get(0);

		final List<Map<String, String>> testData = new ArrayList<Map<String, String>>();
		Map<String, String> datarow = new HashMap<String, String>();
		datarow.put("Id", id);
		datarow.put("FirstName", "Wei");
		datarow.put("LastName", "Wang");
		datarow.put("Phone", "010-89492686");// update the field
		testData.add(datarow);

		datarow = new HashMap<String, String>();
		datarow.put("Id", "not_exist");// should reject
		datarow.put("FirstName", "Who");
		datarow.put("LastName", "Who");
		datarow.put("Phone", "010-89492686");
		testData.add(datarow);

		String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

		// outputbulk part
		TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
				.getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);
		TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file,
				util.getTestSchema4());
		util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema4(), testData);

		// bulkexec part
		TSalesforceBulkExecDefinition defin = (TSalesforceBulkExecDefinition) getComponentService()
				.getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
		TSalesforceBulkExecProperties modelProps = (TSalesforceBulkExecProperties) defin.createRuntimeProperties();
		Reader reader = util.initReader(defin, data_file, modelProps, util.getTestSchema4(), util.getTestSchema4());

		modelProps.outputAction.setValue(TSalesforceBulkExecProperties.OutputAction.UPDATE);

		try {
			IndexedRecordConverter<Object, ? extends IndexedRecord> factory = null;

			for (boolean available = reader.start(); available; available = reader.advance()) {
				try {
					Object data = reader.getCurrent();

					factory = initCurrentData(factory, data);
					IndexedRecord record = factory.convertToAvro(data);

					String resultid = (String) record.get(0);
					String phone = (String) record.get(3);

					Assert.assertEquals(id, resultid);
					Assert.assertEquals("010-89492686", phone);
				} catch (DataRejectException e) {
					java.util.Map<String, Object> info = e.getRejectInfo();
					Object data = info.get("talend_record");
					String err = (String) info.get("error");

					factory = initCurrentData(factory, data);
					IndexedRecord record = factory.convertToAvro(data);

					String resultid = (String) record.get(0);
					String firstname = (String) record.get(1);
					String lastname = (String) record.get(2);
					String phone = (String) record.get(3);

					Assert.assertNull(resultid);
					Assert.assertEquals("Who", firstname);
					Assert.assertEquals("Who", lastname);
					Assert.assertEquals("010-89492686", phone);
					Assert.assertTrue(err != null);
				}
			}
		} finally {
			try {
				reader.close();
			} finally {
				util.deleteTestData(ids);
			}
		}
	}

	@Test
	public void testUpsert() throws Throwable {
		List<String> ids = util.createTestData();

		String id = ids.get(0);

		final List<Map<String, String>> testData = new ArrayList<Map<String, String>>();
		Map<String, String> datarow = new HashMap<String, String>();
		datarow.put("Id", id);// should update
		datarow.put("FirstName", "Wei");
		datarow.put("LastName", "Wang");
		datarow.put("Phone", "010-89492686");// update the field
		testData.add(datarow);

		datarow = new HashMap<String, String>();
		datarow.put("Id", null);// should insert
		datarow.put("FirstName", "Who");
		datarow.put("LastName", "Who");
		datarow.put("Phone", "010-89492686");
		testData.add(datarow);

		String data_file = tempFolder.newFile("data.txt").getAbsolutePath();

		// outputbulk part
		TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
				.getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);
		TSalesforceOutputBulkProperties modelProperties = util.simulateUserBasicAction(definition, data_file,
				util.getTestSchema4());
		util.simulateRuntimeCaller(definition, modelProperties, util.getTestSchema4(), testData);

		// bulkexec part
		TSalesforceBulkExecDefinition defin = (TSalesforceBulkExecDefinition) getComponentService()
				.getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
		TSalesforceBulkExecProperties modelProps = (TSalesforceBulkExecProperties) defin.createRuntimeProperties();
		Reader reader = util.initReader(defin, data_file, modelProps, util.getTestSchema4(), util.getTestSchema4());

		modelProps.outputAction.setValue(TSalesforceBulkExecProperties.OutputAction.UPSERT);
		modelProps.upsertKeyColumn.setValue("Id");

		try {
			IndexedRecordConverter<Object, ? extends IndexedRecord> factory = null;

			int index = -1;
			for (boolean available = reader.start(); available; available = reader.advance()) {
				try {
					Object data = reader.getCurrent();

					factory = initCurrentData(factory, data);
					IndexedRecord record = factory.convertToAvro(data);

					String resultid = (String) record.get(0);
					String phone = (String) record.get(3);

					index++;
					if (index == 0) {
						Assert.assertEquals(id, resultid);
						Assert.assertEquals("010-89492686", phone);
					} else if (index == 1) {
						Assert.assertTrue(resultid != null);
						Assert.assertEquals("010-89492686", phone);
					}
				} catch (DataRejectException e) {
					Assert.fail(e.getMessage());
				}
			}
		} finally {
			try {
				reader.close();
			} finally {
				util.deleteTestData(ids);
			}
		}
	}

    private IndexedRecordConverter<Object, ? extends IndexedRecord> initCurrentData(
            IndexedRecordConverter<Object, ? extends IndexedRecord> factory, Object data) {
        if (factory == null) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(data.getClass());
        }

        // IndexedRecord unenforced = factory.convertToAvro(data);
        // current.setWrapped(unenforced);
        return factory;
    }

}
