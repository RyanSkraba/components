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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.talend6.Talend6IncomingSchemaEnforcer;

/**
 * Created by wwang on 2016-03-09.
 */
public class SalesforceBulkFileWriterTestIT extends SalesforceTestBase {
	
	@Rule  
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	Schema schema = SchemaBuilder.builder().record("Schema").fields()
        .name("id").type().nullable().stringType().noDefault()
        .name("name").type().nullable().stringType().noDefault() 
        .name("address").type().nullable().stringType().noDefault() 
    .endRecord();
	
	@Test
	public void testBasic() throws Exception {
		TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
				.getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);
		
		String data_file = tempFolder.newFile("data.txt").getAbsolutePath();
		
		TSalesforceOutputBulkProperties modelProperties = simulateUserBasicAction(definition, data_file);
        
        String[] expected = {"id","name","address"};
        List actual = modelProperties.upsertRelationTable.columnName.getPossibleValues();
        Assert.assertArrayEquals(expected, actual.toArray());
		
		simulateRuntimeCaller(definition, modelProperties);
		
		String[] expected1 = {"id,name,address","1,Talend,France","2,Google,USA","3,IBM,#N/A"};
		compareFileContent(data_file,expected1);
		
		//test ignore null
		modelProperties.ignoreNull.setValue("true");
		simulateRuntimeCaller(definition, modelProperties);
		
		String[] expected2 = {"id,name,address","1,Talend,France","2,Google,USA","3,IBM,"};
		compareFileContent(data_file,expected2);
		
		//test append
		modelProperties.append.setValue("true");
		simulateRuntimeCaller(definition, modelProperties);
		
		String[] expected3 = {"id,name,address","1,Talend,France","2,Google,USA","3,IBM,","1,Talend,France","2,Google,USA","3,IBM,"};
		compareFileContent(data_file,expected3);
		
	}
	
	@Test
	public void testUpsertAction() throws Exception {
		TSalesforceOutputBulkDefinition definition = (TSalesforceOutputBulkDefinition) getComponentService()
				.getComponentDefinition(TSalesforceOutputBulkDefinition.COMPONENT_NAME);
		
		String data_file = tempFolder.newFile("data.txt").getAbsolutePath();
		
		TSalesforceOutputBulkProperties modelProperties = simulateUserBasicAction(definition, data_file);
        
		java.util.List<String> columnNames = new java.util.ArrayList<String>();
		columnNames.add("address");
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
		
		simulateRuntimeCaller(definition, modelProperties);
		
		String[] expected1 = {"id,name,fmn:lfn.eid","1,Talend,France","2,Google,USA","3,IBM,#N/A"};
		compareFileContent(data_file,expected1);
	}

	private TSalesforceOutputBulkProperties simulateUserBasicAction(TSalesforceOutputBulkDefinition definition,
			String data_file) {
		//simulate some ui action
		//user create component
		TSalesforceOutputBulkProperties modelProperties = (TSalesforceOutputBulkProperties)definition.createProperties();//setup the properties, trigger the setup layout and refresh layout
		
		//user set the schema and file path
        modelProperties.schema.schema.setValue(schema);
        modelProperties.bulkFilePath.setValue(data_file);
        
        //user switch the ui and trigger it
		modelProperties.beforeUpsertRelationTable();
		return modelProperties;
	}

	private void simulateRuntimeCaller(TSalesforceOutputBulkDefinition definition,
			TSalesforceOutputBulkProperties modelProperties) throws IOException {
		//simulate to generate the runtime code
        TSalesforceOutputBulkProperties runtimeProperties = (TSalesforceOutputBulkProperties)definition.createRuntimeProperties();
		//pass all the value from the ui model
        runtimeProperties.schema.schema.setValue(modelProperties.schema.schema.getStringValue());
		runtimeProperties.bulkFilePath.setValue(modelProperties.bulkFilePath.getStringValue());
		runtimeProperties.append.setValue(modelProperties.append.getStringValue());
		runtimeProperties.ignoreNull.setValue(modelProperties.ignoreNull.getStringValue());
		
		Object obj = modelProperties.upsertRelationTable.columnName.getValue();
		if(obj!=null && obj instanceof List && !((List)obj).isEmpty()) {
			runtimeProperties.upsertRelationTable.columnName.setValue(modelProperties.upsertRelationTable.columnName.getValue());
			runtimeProperties.upsertRelationTable.lookupFieldExternalIdName.setValue(modelProperties.upsertRelationTable.lookupFieldExternalIdName.getValue());
			runtimeProperties.upsertRelationTable.lookupFieldModuleName.setValue(modelProperties.upsertRelationTable.lookupFieldModuleName.getValue());
			runtimeProperties.upsertRelationTable.lookupFieldName.setValue(modelProperties.upsertRelationTable.lookupFieldName.getValue());
			runtimeProperties.upsertRelationTable.polymorphic.setValue(modelProperties.upsertRelationTable.polymorphic.getValue());
		}
		
		SourceOrSink source_sink = definition.getRuntime();
		source_sink.initialize(null, runtimeProperties);
		ValidationResult result = source_sink.validate(null);
		Assert.assertTrue(result.getStatus() == ValidationResult.Result.OK);
		
		Sink sink = (Sink)source_sink;
		WriteOperation writeOperation = sink.createWriteOperation();
		writeOperation.initialize(null);
		Writer writer = writeOperation.createWriter(null);
		writer.open("component_instance_id");
		
		Talend6IncomingSchemaEnforcer enforcer = new Talend6IncomingSchemaEnforcer(schema);
		
		try {
			writeSampleData(writer, enforcer);
		} finally {
	    	writer.close();
	    }
	}

	private void writeSampleData(Writer writer, Talend6IncomingSchemaEnforcer enforcer) throws IOException {
		enforcer.put("id", "1");
		enforcer.put("name", "Talend");
		enforcer.put("address", "France");
		Object data = enforcer.createIndexedRecord();
		writer.write(data);
		
		enforcer.put("id", "2");
		enforcer.put("name", "Google");
		enforcer.put("address", "USA");
		data = enforcer.createIndexedRecord();
		writer.write(data);
		
		enforcer.put("id", "3");
		enforcer.put("name", "IBM");
		enforcer.put("address", null);
		data = enforcer.createIndexedRecord();
		writer.write(data);
	}
	
	private void compareFileContent(String path, String[] expected) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			
			int index = 0;
			String row = null;
			while((row = reader.readLine())!=null) {
				Assert.assertEquals(expected[index++],row);
			}
		} finally {
			if(reader!=null) {
				reader.close();
			}
		} 
		
	}

}
