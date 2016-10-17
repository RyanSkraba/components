package org.talend.components.dataprep.tdatasetinput;

import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.components.service.spring.SpringTestApp;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
@WebIntegrationTest("server.port:0")
public class AfterFetchSchemaTest {

    @Inject
    private ComponentService componentService;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Value("${local.server.port}")
    private int serverPort;

    @Test
    public void testAfterFetchSchema() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.getComponentDefinition("tDatasetInput")
                .createProperties();
        properties.url.setValue("http://localhost:" + serverPort);
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.dataSetName.setValue("mydataset");
        properties.dataSetId.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        Assert.assertEquals(Result.OK, properties.afterFetchSchema().getStatus());
    }

    // TODO should remove the duplicated code for afterFetchSchema
    @Test
    public void testAfterFetchSchemaForOutput() {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) componentService.getComponentDefinition("tDatasetOutput")
                .createProperties();
        properties.url.setValue("http://localhost:" + serverPort);
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.dataSetName.setValue("mydataset");
        properties.dataSetId.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        Assert.assertEquals(Result.OK, properties.afterFetchSchema().getStatus());
    }

    @Test
    public void testAfterFetchSchemaFailed() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.getComponentDefinition("tDatasetInput")
                .createProperties();
        properties.url.setValue("http://localhost:" + serverPort);
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("wrong");
        properties.dataSetId.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        Assert.assertEquals(ValidationResult.Result.ERROR, properties.afterFetchSchema().getStatus());
    }

    @Test
    public void testFieldSetting() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.getComponentDefinition("tDatasetInput")
                .createProperties();
        collector.checkThat(properties.afterFetchSchema().getStatus(), equalTo(ValidationResult.Result.ERROR));
        properties.url.setValue("http://localhost:8080");
        collector.checkThat(properties.afterFetchSchema().getStatus(), equalTo(ValidationResult.Result.ERROR));
        properties.login.setValue("vincent@dataprep.com");
        collector.checkThat(properties.afterFetchSchema().getStatus(), equalTo(ValidationResult.Result.ERROR));
        properties.pass.setValue("wrong");
        collector.checkThat(properties.afterFetchSchema().getStatus(), equalTo(ValidationResult.Result.ERROR));
        properties.dataSetId.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
    }
}
