package org.talend.components.dataprep;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringApp;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputDefinition;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;

import javax.inject.Inject;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class TDataSetOutputSinkTest {

        @Inject
        private ComponentService componentService;

        private TDataSetOutputSink outputSink;

        private TDataSetOutputDefinition definition;

    @Before
    public void setInputSource() {
        definition = (TDataSetOutputDefinition) (componentService.getComponentDefinition("tDatasetOutput"));
        outputSink = (TDataSetOutputSink) definition.getRuntime();
    }

    @Test
    public void testCreateWriteOperation() throws Exception {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) definition.createProperties();
        properties.setValue("url", "http://127.0.0.1");
        outputSink.initialize(null, properties);
        outputSink.createWriteOperation();
    }

    @Test
    public void testInitialize() throws Exception {

    }

    @Test
    public void testValidate() throws Exception {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) definition.createProperties();
        properties.setValue("url", "http://127.0.0.1");
        outputSink.initialize(null, properties);
        outputSink.validate(null);
    }

    @Test
    public void validateLiveDataSet() {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) definition.createProperties();
        properties.setValue("url", "http://127.0.0.1");
        properties.setValue("mode", properties.LIVE_DATASET);
        outputSink.initialize(null, properties);
        outputSink.validate(null);
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        Assert.assertEquals(Collections.EMPTY_LIST, outputSink.getSchemaNames(null));

    }

    @Test
    public void testGetSchema() throws Exception {
        Assert.assertNull(outputSink.getSchema(null,null));
    }
}