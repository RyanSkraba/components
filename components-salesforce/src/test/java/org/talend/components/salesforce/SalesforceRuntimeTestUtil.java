package org.talend.components.salesforce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.runtime.SalesforceSource;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.talend6.Talend6IncomingSchemaEnforcer;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceRuntimeTestUtil {

	private PartnerConnection partnerConnection;
	
	private final Schema schema = SchemaBuilder.builder().record("Schema").fields()
	    .name("FirstName").type().nullable().stringType().noDefault()
	    .name("LastName").type().nullable().stringType().noDefault() 
	    .name("Phone").type().nullable().stringType().noDefault() 
	.endRecord();
	
	private final String module = "Contact";
	
	private final List<Map<String,String>> testData = new ArrayList<Map<String,String>>();
	
	{
		Map<String,String> row = new HashMap<String,String>();
		row.put("FirstName", "Wei");
		row.put("LastName", "Wang");
		row.put("Phone", "010-11111111");
		testData.add(row);
		
		row = new HashMap<String,String>();
		row.put("FirstName", "Jin");
		row.put("LastName", "Zhao");
		row.put("Phone", "010-11111112");
		testData.add(row);
		
		row = new HashMap<String,String>();
		row.put("FirstName", "Wei");
		row.put("LastName", "Yuan");
		row.put("Phone", null);
		testData.add(row);
	}
	
	private final String username = System.getProperty("salesforce.user");
	private final String password = System.getProperty("salesforce.password");
	private final String key = System.getProperty("salesforce.key");
	
	public Schema getTestSchema() {
		return schema;
	}
	
	public List<Map<String,String>> getTestData() {
		return testData;
	}
	
    public void login(String endpoint) throws ConnectionException {
    	ConnectorConfig config = new ConnectorConfig();
    	
    	config.setAuthEndpoint(endpoint);
    	config.setUsername(username);
        config.setPassword(password + key);
        config.setConnectionTimeout(60000);
        config.setUseChunkedPost(true);
        
        partnerConnection = new PartnerConnection(config);
    }
    
    public List<String> createTestData() {
        List<String> ids = new ArrayList<String>();
        try {
        	List<SObject> contacts = new ArrayList<SObject>();
        	
        	for(Map<String,String> row : testData) {
        		SObject contact = new SObject();
	            contact.setType(module);
	            contact.setField("FirstName",row.get("FirstName"));
	            contact.setField("LastName",row.get("LastName"));
	            contact.setField("Phone", row.get("Phone"));
	            contacts.add(contact);
        	}
            
            SaveResult[] results = partnerConnection.create(contacts.toArray(new SObject[0]));
        
            for (int j = 0; j < results.length; j++) {
                if (results[j].isSuccess()) {
                	ids.add(results[j].getId());
                 } else {
                    for (int i = 0; i < results[j].getErrors().length; i++) {
                        com.sforce.soap.partner.Error err = results[j].getErrors()[i];
                        Assert.fail(err.getMessage());
                    }
                 }
            }
        } catch (ConnectionException ce) {
        	Assert.fail(ce.getMessage());
        }
        return ids;
    }
    
    public void deleteTestData(List<String> ids) throws ConnectionException {
        try {
            DeleteResult[] results = partnerConnection.delete(ids.toArray(new String[0]));
            
            for (int j = 0; j < results.length; j++) {
                if (results[j].isSuccess()) {
                	
                 } else {
                    for (int i = 0; i < results[j].getErrors().length; i++) {
                        com.sforce.soap.partner.Error err = results[j].getErrors()[i];
                        Assert.fail(err.getMessage());
                    }
                 }
            }
        } catch (ConnectionException ce) {
        	Assert.fail(ce.getMessage());
        }
    }
    
    public void compareFileContent(String path, String[] expected) throws IOException {
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
    
    public TSalesforceOutputBulkProperties simulateUserBasicAction(TSalesforceOutputBulkDefinition definition,
			String data_file) {
		//simulate some ui action
		//user create component
		TSalesforceOutputBulkProperties modelProperties = (TSalesforceOutputBulkProperties)definition.createProperties();//setup the properties, trigger the setup layout and refresh layout
		
		//user set the schema and file path
        modelProperties.schema.schema.setValue(this.getTestSchema());
        modelProperties.bulkFilePath.setValue(data_file);
        
        //user switch the ui and trigger it
		modelProperties.beforeUpsertRelationTable();
		return modelProperties;
	}

	public void simulateRuntimeCaller(TSalesforceOutputBulkDefinition definition,
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
		
		Talend6IncomingSchemaEnforcer enforcer = new Talend6IncomingSchemaEnforcer(this.getTestSchema());
		
		List<Map<String,String>> testData = this.getTestData();
		try {
			
			for(Map<String,String> row : testData) {
				enforcer.put("FirstName",row.get("FirstName"));
				enforcer.put("LastName",row.get("LastName"));
				enforcer.put("Phone", row.get("Phone"));
				Object data = enforcer.createIndexedRecord();
				writer.write(data);
        	}
		} finally {
	    	writer.close();
	    }
	}
	
	public void simulateRuntimeCaller(TSalesforceBulkExecDefinition definition,
			String data_file) throws IOException, ConnectionException {
		TSalesforceBulkExecProperties modelProperties = (TSalesforceBulkExecProperties)definition.createRuntimeProperties();
		
		ComponentProperties userPassword = (ComponentProperties) modelProperties.getProperty("userPassword");
        ((Property) userPassword.getProperty("userId")).setValue(username);
        ((Property) userPassword.getProperty("password")).setValue(password);
        ((Property) userPassword.getProperty("securityKey")).setValue(key);
        
		modelProperties.connection.timeout.setValue(120000);
		modelProperties.connection.bulkConnection.setValue("true");
		modelProperties.outputAction.setValue(TSalesforceBulkExecProperties.OutputAction.INSERT);
        modelProperties.bulkFilePath.setValue(data_file);
        modelProperties.bulkProperties.bytesToCommit.setValue(10 * 1024 * 1024);
        modelProperties.bulkProperties.rowsToCommit.setValue(10000);
        modelProperties.bulkProperties.concurrencyMode.setValue(SalesforceBulkProperties.CONCURRENCY_PARALLEL);
        modelProperties.bulkProperties.waitTimeCheckBatchState.setValue(10000);
        
        modelProperties.module.moduleName.setValue(module);
        modelProperties.module.main.schema.setValue(this.getTestSchema());
        
        SalesforceSource source = (SalesforceSource) definition.getRuntime();
        source.initialize(null, modelProperties);
        BoundedReader reader = source.createReader(null);

        List<String> ids = new ArrayList<String>();
        try {
            boolean hasRecord = reader.start();
            List<IndexedRecord> rows = new ArrayList<>();
            while (hasRecord) {
                rows.add((IndexedRecord) reader.getCurrent());
                hasRecord = reader.advance();
            }
        } finally {
        	try {
        		reader.close();
        	} finally {
        		this.deleteTestData(ids);
        	}
        }
	}
    
}
