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
import org.talend.components.api.test.SpringTestApp;
import org.talend.daikon.properties.ValidationResult;

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
        properties.url.setValue("http://localhost:"+serverPort);
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("vincent");
        properties.dataSetName.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        Assert.assertEquals(ValidationResult.OK, properties.afterFetchSchema());
    }

    @Test
    public void testAfterFetchSchemaFailed() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.getComponentDefinition("tDatasetInput")
                .createProperties();
        properties.url.setValue("http://localhost:"+serverPort);
        properties.login.setValue("vincent@dataprep.com");
        properties.pass.setValue("wrong");
        properties.dataSetName.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
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
        properties.dataSetName.setValue("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
    }
}
