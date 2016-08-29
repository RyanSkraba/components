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
package org.talend.components.datastewardship.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.common.TdsConstants;
import org.talend.components.datastewardship.runtime.writer.TdsTaskWriter;
import org.talend.components.datastewardship.tdatastewardshiptaskoutput.TDataStewardshipTaskOutputDefinition;
import org.talend.components.datastewardship.tdatastewardshiptaskoutput.TDataStewardshipTaskOutputProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
@WebIntegrationTest("server.port:0")
@SuppressWarnings("nls")
public class TdsTaskWriterTest {

    @Inject
    private ComponentService componentService;

    private TdsTaskWriter writer;

    private TDataStewardshipTaskOutputProperties properties;

    private TdsTaskSink sink;

    @Value("${local.server.port}")
    private int serverPort;

    @Before
    public void setDefaultValues() {
        TDataStewardshipTaskOutputDefinition definition = (TDataStewardshipTaskOutputDefinition) componentService
                .getComponentDefinition("tDataStewardshipTaskOutput");
        properties = (TDataStewardshipTaskOutputProperties) definition.createProperties();
        properties.connection.url.setValue("http://localhost:" + serverPort);
        properties.connection.username.setValue("owner1");
        properties.connection.password.setValue("owner1");
        properties.campaignName.setValue("perf-review-resolution");
        properties.tasksMetadata.taskPriority.setValue(TdsConstants.PRIORITY_MEDIUM);
        properties.tasksMetadata.taskTags.setValue("tagValue");
        properties.tasksMetadata.taskState.setValue("Performance_review_request");
        properties.tasksMetadata.taskAssignee.setValue("user1");
        properties.tasksMetadata.taskComment.setValue("comment");
        properties.batchSize.setValue(0);
        sink = (TdsTaskSink) definition.getRuntime();
    }

    @Test
    public void testWrite() throws IOException {
        properties.campaignType.setValue(CampaignType.RESOLUTION);

        sink.initialize(null, properties);
        TdsTaskWriteOperation writeOperation = (TdsTaskWriteOperation) sink.createWriteOperation();
        writer = (TdsTaskWriter) writeOperation.createWriter(null);

        IndexedRecord record = createIndexedRecord();
        writer.open("testWrite");
        for (int i = 0; i < 10; i++) {
            writer.write(record);
        }

        Result result = writer.close();
        List<Result> results = new ArrayList<>();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);
        Assert.assertEquals(10, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }
 
    private IndexedRecord createIndexedRecord() {
        Integer random = (int)(Math.random() * 10000);
        Schema schema = createSchema();
        IndexedRecord record = new GenericData.Record(schema);
        record.put(schema.getField("CampanyId").pos(), "10001");
        record.put(schema.getField("SocialSecurityId").pos(), random);
        record.put(schema.getField("FirstName").pos(), "Dev" + random);
        record.put(schema.getField("LastName").pos(), "Lee");
        record.put(schema.getField("BirthDate").pos(), 19820507);
        record.put(schema.getField("Team").pos(), "Team");
        record.put(schema.getField("Manager").pos(), "Manager");
        record.put(schema.getField("SelfScore").pos(), 80);
        record.put(schema.getField("ManagerScore").pos(), 85);
        record.put(schema.getField("HRScore").pos(), 90);
        record.put(schema.getField("Position").pos(), "Enginer");
        record.put(schema.getField("Joined").pos(), 20140502);
        record.put(schema.getField("Experience").pos(), "10 Years");
        record.put(schema.getField("Salary").pos(), "20000");
        return record;
    }
    
    public static Schema createSchema() {
        AvroRegistry avroReg = new AvroRegistry();
        SchemaBuilder.FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        addField(record, "CampanyId", String.class, avroReg);
        addField(record, "SocialSecurityId", Integer.class, avroReg);
        addField(record, "FirstName", String.class, avroReg);
        addField(record, "LastName", String.class, avroReg);
        addField(record, "BirthDate", Long.class, avroReg);
        addField(record, "Team", String.class, avroReg);
        addField(record, "Manager", String.class, avroReg);
        addField(record, "SelfScore", Integer.class, avroReg);
        addField(record, "ManagerScore", Integer.class, avroReg);
        addField(record, "HRScore", Integer.class, avroReg);
        addField(record, "Position", String.class, avroReg);
        addField(record, "Joined", Long.class, avroReg);
        addField(record, "Experience", String.class, avroReg);
        addField(record, "Salary", Integer.class, avroReg);
        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }
    
    @Test
    public void testWriteMergingTasks() throws IOException {       
        properties.campaignType.setValue(CampaignType.MERGING);
        properties.advancedMappings.groupIdColumn.setValue("groupId");
        properties.advancedMappings.sourceColumn.setValue("source");
        properties.advancedMappings.masterColumn.setValue("master");
        properties.advancedMappings.scoreColumn.setValue("score");

        sink.initialize(null, properties);
        TdsTaskWriteOperation writeOperation = (TdsTaskWriteOperation) sink.createWriteOperation();
        writer = (TdsTaskWriter) writeOperation.createWriter(null);

        List<IndexedRecord> records = createMergingTasksRecords();
        writer.open("testWrite");
        for (IndexedRecord record : records) {
            writer.write(record);
        }

        Result result = writer.close();
        List<Result> results = new ArrayList<>();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);
        Assert.assertEquals(2, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        Assert.assertEquals(1, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
    }
    
    private List<IndexedRecord> createMergingTasksRecords() {
        List<IndexedRecord> records = new ArrayList<>();
        Schema schema = createMergingTasksSchema();
        IndexedRecord record = new GenericData.Record(schema);
        record.put(schema.getField("Id").pos(), "10001");
        record.put(schema.getField("FirstName").pos(), "Dev");
        record.put(schema.getField("LastName").pos(), "Lee");
        record.put(schema.getField("BirthDate").pos(), 19820507);
        record.put(schema.getField("Salary").pos(), "20000");
        record.put(schema.getField("BankAccount").pos(), "123456789");
        
        record.put(schema.getField("groupId").pos(), "1");
        record.put(schema.getField("source").pos(), "");
        record.put(schema.getField("master").pos(), true);
        record.put(schema.getField("score").pos(), "200");
        records.add(record);
        IndexedRecord record1 = new GenericData.Record(schema);
        record.put(schema.getField("Id").pos(), "10002");
        record.put(schema.getField("FirstName").pos(), "Dev");
        record.put(schema.getField("LastName").pos(), "Lee");
        record.put(schema.getField("BirthDate").pos(), 19820507);
        record.put(schema.getField("Salary").pos(), "20000");
        record.put(schema.getField("BankAccount").pos(), "123456789");
        
        record1.put(schema.getField("groupId").pos(), "1");
        record1.put(schema.getField("source").pos(), "");
        record1.put(schema.getField("master").pos(), false);
        record1.put(schema.getField("score").pos(), "200");
        records.add(record1);
           
        return records;
    }
    
    public static Schema createMergingTasksSchema() {
        AvroRegistry avroReg = new AvroRegistry();
        SchemaBuilder.FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        addField(record, "Id", Integer.class, avroReg);
        addField(record, "SocialSecurityId", Integer.class, avroReg);
        addField(record, "FirstName", String.class, avroReg);
        addField(record, "LastName", String.class, avroReg);
        addField(record, "BirthDate", Long.class, avroReg);
        addField(record, "Salary", Integer.class, avroReg);
        addField(record, "BankAccount", String.class, avroReg);
    
        addField(record, "groupId", String.class, avroReg);
        addField(record, "source", String.class, avroReg);
        addField(record, "master", String.class, avroReg);
        addField(record, "score", String.class, avroReg);
        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }

    private static SchemaBuilder.FieldAssembler<Schema> addField(SchemaBuilder.FieldAssembler<Schema> record, String name, Class<?> type,
            AvroRegistry avroReg) {
        Schema base = avroReg.getConverter(type).getSchema();
        SchemaBuilder.FieldBuilder<Schema> fieldBuilder = record.name(name);
        fieldBuilder.type(AvroUtils.wrapAsNullable(base)).noDefault();
        return record;
    }

}
