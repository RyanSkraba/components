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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.talend.components.datastewardship.runtime.writer.TdsCampaignWriter;
import org.talend.components.datastewardship.tdatastewardshipcampaigncreate.TDataStewardshipCampaignCreateDefinition;
import org.talend.components.datastewardship.tdatastewardshipcampaigncreate.TDataStewardshipCampaignCreateProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;

@SuppressWarnings("nls")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
@WebIntegrationTest("server.port:0")
public class TdsCampaignWriterTest {

    @Inject
    private ComponentService componentService;

    private TdsCampaignWriter writer;

    private TDataStewardshipCampaignCreateProperties properties;

    private TdsCampaignSink sink;

    @Value("${local.server.port}")
    private int serverPort;

    @Before
    public void setDefaultValues() {
        TDataStewardshipCampaignCreateDefinition definition = (TDataStewardshipCampaignCreateDefinition) componentService
                .getComponentDefinition("tDataStewardshipCampaignCreate");
        properties = (TDataStewardshipCampaignCreateProperties) definition.createProperties();
        properties.connection.url.setValue("http://localhost:" + serverPort);
        properties.connection.username.setValue("owner1");
        properties.connection.password.setValue("owner1");
        sink = (TdsCampaignSink) definition.getRuntime();
    }

    @Test
    public void testWrite() throws IOException {
        sink.initialize(null, properties);
        TdsCampaignWriteOperation writeOperation = (TdsCampaignWriteOperation) sink.createWriteOperation();
        writer = (TdsCampaignWriter) writeOperation.createWriter(null);

        writer.open("testWrite");
        
        writer.write(createIndexedRecord("arbitration"));
        writer.write(createIndexedRecord("grouping"));
        writer.write(createIndexedRecord("merging"));
        writer.write(createIndexedRecord("resolution"));

        Result result = writer.close();
        List<Result> results = new ArrayList<Result>();
        results.add(result);
        Map<String, Object> resultMap = writeOperation.finalize(results, null);
        Assert.assertEquals(4, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }

    private IndexedRecord createIndexedRecord(String type) throws IOException {
        Schema schema = createSchema();
        IndexedRecord record = new GenericData.Record(schema);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = this.getClass().getResourceAsStream("campaign-" + type + ".json");
        int i=-1; 
        while((i=is.read())!=-1){ 
            baos.write(i);
        } 
       
        record.put(schema.getField("campaign").pos(), baos.toString());

        return record;
    }
    
    private Schema createSchema() {
        AvroRegistry avroReg = new AvroRegistry();
        SchemaBuilder.FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        addField(record, "campaign", String.class, avroReg);

        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }

    private SchemaBuilder.FieldAssembler<Schema> addField(SchemaBuilder.FieldAssembler<Schema> record, String name, Class<?> type,
            AvroRegistry avroReg) {
        Schema base = avroReg.getConverter(type).getSchema();
        SchemaBuilder.FieldBuilder<Schema> fieldBuilder = record.name(name);
        fieldBuilder.type(AvroUtils.wrapAsNullable(base)).noDefault();
        return record;
    }

}
