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

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.runtime.reader.TdsTaskReader;
import org.talend.components.datastewardship.tdatastewardshiptaskinput.TDataStewardshipTaskInputDefinition;
import org.talend.components.datastewardship.tdatastewardshiptaskinput.TDataStewardshipTaskInputProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
@WebIntegrationTest("server.port:0")
@SuppressWarnings("nls")
public class TdsTaskReaderTest {

    @Inject
    private ComponentService componentService;

    private TdsTaskReader reader;

    private TDataStewardshipTaskInputProperties properties;

    private TdsTaskSource source;

    @Value("${local.server.port}")
    private int serverPort;

    @Before
    public void setDefaultValues() {
        TDataStewardshipTaskInputDefinition definition = (TDataStewardshipTaskInputDefinition) componentService
                .getComponentDefinition("tDataStewardshipTaskInput");
        properties = (TDataStewardshipTaskInputProperties) definition.createProperties();
        properties.connection.url.setValue("http://localhost:" + serverPort);
        properties.connection.username.setValue("owner1");
        properties.connection.password.setValue("owner1");
        properties.campaignName.setValue("perf-review-resolution");
        properties.searchCriteria.taskState.setValue("Performance_review_request");
        properties.schema.schema.setValue(TdsTaskWriterTest.createSchema());
        properties.batchSize.setValue(0);
        properties.goldenOnly.setValue(true);
        source = (TdsTaskSource) definition.getRuntime();
    }

    @Test
    public void testRead() throws IOException { 
        //query resolution tasks
        source.initialize(null, properties);
        reader = (TdsTaskReader) source.createReader(null);
        reader.start();
        Assert.assertEquals(100, reader.getReturnValues().get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));       
        //query merging tasks and goldenOnly = true
        properties.campaignName.setValue("campaign1");
        properties.searchCriteria.taskState.setValue("HR_review_requested");
        properties.campaignType.setValue(CampaignType.MERGING);
        properties.schema.schema.setValue(TdsTaskWriterTest.createMergingTasksSchema());       
        source.initialize(null, properties);
        reader = (TdsTaskReader) source.createReader(null);
        reader.start();     
        Assert.assertEquals(10, reader.getReturnValues().get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        //query merging tasks and goldenOnly = false
        properties.goldenOnly.setValue(false);
        source.initialize(null, properties);
        reader = (TdsTaskReader) source.createReader(null);
        reader.start();
        Assert.assertEquals(30, reader.getReturnValues().get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));        
    }       
    
}
