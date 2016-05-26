package org.talend.components.dataprep.runtime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringApp;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputDefinition;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputProperties;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
@WebIntegrationTest
public class DataSetReaderTest {

    @Inject
    private ComponentService componentService;

    private DataSetReader reader;

    @Before
    public void setReader() {
        TDataSetInputDefinition definition = (TDataSetInputDefinition) componentService.getComponentDefinition("tDatasetInput");
        TDataSetInputProperties properties = (TDataSetInputProperties) definition.createProperties();
        properties.url.setValue("http://localhost:8080");
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.dataSetName.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        DataSetSource source = (DataSetSource) definition.getRuntime();
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