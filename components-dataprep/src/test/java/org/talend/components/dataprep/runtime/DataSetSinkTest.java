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
package org.talend.components.dataprep.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.service.ComponentService;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputDefinition;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.components.service.spring.SpringTestApp;
import org.talend.daikon.properties.ValidationResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class DataSetSinkTest {

    @Inject
    private ComponentService componentService;

    private DataSetSink outputSink;

    private TDataSetOutputDefinition definition;

    @Before
    public void setInputSource() {
        definition = (TDataSetOutputDefinition) (componentService.getComponentDefinition("tDatasetOutput"));
        outputSink = new DataSetSink();
    }

    @Test
    public void testCreateWriteOperation() throws Exception {
        WriteOperation<?> writeOperation = outputSink.createWriteOperation();
        assertThat(writeOperation, instanceOf(DataSetWriteOperation.class));
    }

    @Test
    public void testInitialize() throws Exception {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) definition.createProperties();
        properties.setValue("url", "http://127.0.0.1");
        properties.setValue("mode", DataPrepOutputModes.LiveDataset);
        Assert.assertSame(ValidationResult.OK, outputSink.initialize(null, properties));

    }

    @Test
    public void testValidate() throws Exception {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) definition.createProperties();
        properties.setValue("url", "http://127.0.0.1");
        outputSink.initialize(null, properties);
        Assert.assertNotSame(ValidationResult.OK, outputSink.validate(null));
    }

    @Test
    public void testValidateLiveDataSet() {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) definition.createProperties();
        properties.setValue("url", "http://127.0.0.1");
        properties.setValue("mode", DataPrepOutputModes.LiveDataset);
        outputSink.initialize(null, properties);
        Assert.assertSame(ValidationResult.OK, outputSink.validate(null));
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        Assert.assertEquals(Collections.EMPTY_LIST, outputSink.getSchemaNames(null));

    }

    @Test
    public void testGetSchema() throws Exception {
        Assert.assertNull(outputSink.getEndpointSchema(null, null));
    }
}