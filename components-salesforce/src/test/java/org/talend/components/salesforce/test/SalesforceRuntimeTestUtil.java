// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.salesforce.SalesforceBulkProperties.Concurrency;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.runtime.SalesforceBulkFileSink;
import org.talend.components.salesforce.runtime.SalesforceSource;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.properties.ValidationResult;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceRuntimeTestUtil {

    private PartnerConnection partnerConnection;

    private final Schema schema1 = SchemaBuilder.builder().record("Schema").fields().name("FirstName").type().nullable()
            .stringType().noDefault().name("LastName").type().nullable().stringType().noDefault().name("Phone").type().nullable()
            .stringType().noDefault().endRecord();

    private final Schema schema2 = SchemaBuilder.builder().record("Schema").fields().name("Id").type().stringType().noDefault()
            .name("FirstName").type().nullable().stringType().noDefault().name("LastName").type().nullable().stringType()
            .noDefault().name("Phone").type().nullable().stringType().noDefault().name("salesforce_id").type().stringType()
            .noDefault().endRecord();

    private final Schema schema3 = SchemaBuilder.builder().record("Schema").fields().name("Id").type().stringType().noDefault()
            .endRecord();

    private final Schema schema4 = SchemaBuilder.builder().record("Schema").fields().name("Id").type().stringType().noDefault()
            .name("FirstName").type().nullable().stringType().noDefault().name("LastName").type().nullable().stringType()
            .noDefault().name("Phone").type().nullable().stringType().noDefault().endRecord();

    private final String module = "Contact";

    private final List<Map<String, String>> testData = new ArrayList<Map<String, String>>();

    {
        Map<String, String> row = new HashMap<String, String>();
        row.put("FirstName", "Wei");
        row.put("LastName", "Wang");
        row.put("Phone", "010-11111111");
        testData.add(row);

        row = new HashMap<String, String>();
        row.put("FirstName", "Jin");
        row.put("LastName", "Zhao");
        row.put("Phone", "010-11111112");
        testData.add(row);

        row = new HashMap<String, String>();
        row.put("FirstName", "Wei");
        row.put("LastName", "Yuan");
        row.put("Phone", null);
        testData.add(row);
    }

    private final String username = System.getProperty("salesforce.user");

    private final String password = System.getProperty("salesforce.password");

    private final String securityKey = System.getProperty("salesforce.key");

    public Schema getTestSchema1() {
        return schema1;
    }

    public Schema getTestSchema2() {
        return schema2;
    }

    public Schema getTestSchema3() {
        return schema3;
    }

    public Schema getTestSchema4() {
        return schema4;
    }

    public List<Map<String, String>> getTestData() {
        return testData;
    }

    public String getTestModuleName() {
        return module;
    }

    private void login(String endpoint) throws ConnectionException {
        ConnectorConfig config = new ConnectorConfig();

        config.setAuthEndpoint(endpoint);
        config.setUsername(username);
        config.setPassword(password + securityKey);
        config.setConnectionTimeout(60000);
        config.setUseChunkedPost(true);

        partnerConnection = new PartnerConnection(config);
    }

    public List<String> createTestData() throws ConnectionException {
        this.login(SalesforceConnectionProperties.URL);

        List<String> ids = new ArrayList<String>();
        try {
            List<SObject> contacts = new ArrayList<SObject>();

            for (Map<String, String> row : testData) {
                SObject contact = new SObject();
                contact.setType(module);
                contact.setField("FirstName", row.get("FirstName"));
                contact.setField("LastName", row.get("LastName"));
                contact.setField("Phone", row.get("Phone"));
                contacts.add(contact);
            }

            SaveResult[] results = partnerConnection.create(contacts.toArray(new SObject[0]));

            for (SaveResult result : results) {
                if (result.isSuccess()) {
                    ids.add(result.getId());
                } else {
                    for (int i = 0; i < result.getErrors().length; i++) {
                        com.sforce.soap.partner.Error err = result.getErrors()[i];
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
        this.login(SalesforceConnectionProperties.URL);

        try {
            DeleteResult[] results = partnerConnection.delete(ids.toArray(new String[0]));

            for (DeleteResult result : results) {
                if (result.isSuccess()) {

                } else {
                    for (int i = 0; i < result.getErrors().length; i++) {
                        com.sforce.soap.partner.Error err = result.getErrors()[i];
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
            while ((row = reader.readLine()) != null) {
                Assert.assertEquals(expected[index++], row);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

    }

    public TSalesforceOutputBulkProperties simulateUserBasicAction(TSalesforceOutputBulkDefinition definition, String data_file,
            Schema schema) {
        // simulate some ui action
        // user create component
        TSalesforceOutputBulkProperties modelProperties = (TSalesforceOutputBulkProperties) definition.createProperties();// setup
                                                                                                                          // the
                                                                                                                          // properties,
                                                                                                                          // trigger
                                                                                                                          // the
                                                                                                                          // setup
                                                                                                                          // layout
                                                                                                                          // and
                                                                                                                          // refresh
                                                                                                                          // layout

        // user set the schema and file path
        modelProperties.schema.schema.setValue(schema);
        modelProperties.bulkFilePath.setValue(data_file);

        // user switch the ui and trigger it
        modelProperties.beforeUpsertRelationTable();
        return modelProperties;
    }

    public void simulateRuntimeCaller(TSalesforceOutputBulkDefinition definition, TSalesforceOutputBulkProperties modelProperties,
            Schema schema, List<Map<String, String>> rows) throws IOException {
        Writer writer = initWriter(definition, modelProperties);

        try {
            for (Map<String, String> row : rows) {
                GenericRecord record = new GenericData.Record(schema);
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    record.put(entry.getKey(), entry.getValue());
                }
                writer.write(record);
            }
        } finally {
            writer.close();
        }
    }

    private Writer initWriter(TSalesforceOutputBulkDefinition definition, TSalesforceOutputBulkProperties modelProperties)
            throws IOException {
        // simulate to generate the runtime code
        TSalesforceOutputBulkProperties runtimeProperties = (TSalesforceOutputBulkProperties) definition
                .createRuntimeProperties();
        // pass all the value from the ui model
        runtimeProperties.schema.schema.setValue(modelProperties.schema.schema.getValue());
        runtimeProperties.bulkFilePath.setValue(modelProperties.bulkFilePath.getStringValue());
        runtimeProperties.append.setValue(modelProperties.append.getValue());
        runtimeProperties.ignoreNull.setValue(modelProperties.ignoreNull.getValue());

        Object obj = modelProperties.upsertRelationTable.columnName.getValue();
        if (obj != null && obj instanceof List && !((List) obj).isEmpty()) {
            runtimeProperties.upsertRelationTable.columnName.setValue(modelProperties.upsertRelationTable.columnName.getValue());
            runtimeProperties.upsertRelationTable.lookupFieldExternalIdName
                    .setValue(modelProperties.upsertRelationTable.lookupFieldExternalIdName.getValue());
            runtimeProperties.upsertRelationTable.lookupFieldModuleName
                    .setValue(modelProperties.upsertRelationTable.lookupFieldModuleName.getValue());
            runtimeProperties.upsertRelationTable.lookupRelationshipFieldName
                    .setValue(modelProperties.upsertRelationTable.lookupRelationshipFieldName.getValue());
            runtimeProperties.upsertRelationTable.polymorphic
                    .setValue(modelProperties.upsertRelationTable.polymorphic.getValue());
        }

        SourceOrSink source_sink = new SalesforceBulkFileSink();
        source_sink.initialize(null, runtimeProperties);
        ValidationResult result = source_sink.validate(null);
        Assert.assertTrue(result.getStatus() == ValidationResult.Result.OK);

        Sink sink = (Sink) source_sink;
        WriteOperation writeOperation = sink.createWriteOperation();
        writeOperation.initialize(null);
        Writer writer = writeOperation.createWriter(null);
        writer.open("component_instance_id");
        return writer;
    }

    public Reader initReader(TSalesforceBulkExecDefinition definition, String data_file,
            TSalesforceBulkExecProperties modelProperties, Schema schema, Schema output) {
        modelProperties.connection.userPassword.userId.setValue(username);
        modelProperties.connection.userPassword.password.setValue(password);
        modelProperties.connection.userPassword.securityKey.setValue(securityKey);

        modelProperties.connection.timeout.setValue(60000);
        modelProperties.connection.bulkConnection.setValue(true);
        modelProperties.bulkFilePath.setValue(data_file);
        modelProperties.bulkProperties.bytesToCommit.setValue(10 * 1024 * 1024);
        modelProperties.bulkProperties.rowsToCommit.setValue(10000);
        modelProperties.bulkProperties.concurrencyMode.setValue(Concurrency.Parallel);
        modelProperties.bulkProperties.waitTimeCheckBatchState.setValue(10000);

        modelProperties.module.moduleName.setValue(module);
        modelProperties.module.main.schema.setValue(schema);
        modelProperties.schemaFlow.schema.setValue(output);

        Source source = new SalesforceSource();
        source.initialize(null, modelProperties);
        ValidationResult vr = source.validate(null);
        if (vr.getStatus() == ValidationResult.Result.ERROR) {
            Assert.fail(vr.getMessage());
        }

        Reader reader = source.createReader(null);
        return reader;
    }

}
