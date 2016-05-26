package org.talend.components.dataprep.runtime;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringApp;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputDefinition;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
@WebIntegrationTest
@Ignore
public class DataSetWriterTest {

    @Inject
    private ComponentService componentService;

    private DataSetWriter writer;
    private TDataSetOutputProperties properties;

    @Before
    public void setWriter() {
        TDataSetOutputDefinition definition = (TDataSetOutputDefinition) componentService
                .getComponentDefinition("tDatasetOutput");
        properties = (TDataSetOutputProperties) definition.createProperties();
        properties.url.setValue("http://localhost:8080");
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.dataSetName.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        properties.mode.setValue(DataPrepOutputModes.CREATE);
        properties.limit.setValue("10");
        DataSetSink sink = (DataSetSink) definition.getRuntime();
        writer = (DataSetWriter) sink.createWriteOperation().createWriter(null);
    }

    @Test
    public void testWriter() {

    }
}