package org.talend.components.dataprep.runtime;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputDefinition;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
@WebIntegrationTest("server.port:0")
public class DataSetReaderTest {

    @Inject
    private ComponentService componentService;

    private DataSetReader reader;

    @Value("${local.server.port}")
    private int serverPort;

    @Before
    public void setReader() {
        TDataSetInputDefinition definition = (TDataSetInputDefinition) componentService.getComponentDefinition("tDatasetInput");
        TDataSetInputProperties properties = (TDataSetInputProperties) definition.createProperties();
        properties.url.setValue("http://localhost:" + serverPort);
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.dataSetId.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        DataSetSource source = new DataSetSource();
        source.initialize(null, properties);
        reader = (DataSetReader) source.createReader(null);
    }

    @Test
    public void testStart() throws Exception {
        Assert.assertTrue(reader.start());
        while (reader.advance()) {
            Assert.assertNotNull(reader.getCurrent());
        }
        reader.close();
    }
}