package org.talend.components.dataprep.runtime;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputDefinition;
import org.talend.components.dataprep.tdatasetinput.TDataSetInputProperties;
import org.talend.components.service.spring.SpringTestApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class DataSetSourceTest {

    @Inject
    private ComponentService componentService;

    private DataSetSource inputSource;

    private TDataSetInputDefinition definition;

    @Before
    public void setInputSource() {
        definition = (TDataSetInputDefinition) (componentService.getComponentDefinition("tDatasetInput"));
        inputSource = new DataSetSource();
    }

    @Test
    public void testCreateReader() throws Exception {
        inputSource.initialize(null, definition.createProperties());
        Assert.assertNotNull(inputSource.createReader(null));
    }

    @Test
    public void testValidateThrowsException() throws Exception {

        TDataSetInputProperties properties = (TDataSetInputProperties) definition.createProperties();
        properties.setValue("url", "http://127.0.0.1");
        inputSource.initialize(null, properties);
        inputSource.validate(null);
    }

    @Test
    public void testGetSchema() throws Exception {
        Assert.assertNull(inputSource.getEndpointSchema(null, null));
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        Assert.assertEquals(Collections.emptyList(), inputSource.getSchemaNames(null));
    }

    @Test
    public void testSplitIntoBundles() throws Exception {
        Assert.assertEquals(Arrays.asList(inputSource), inputSource.splitIntoBundles(0, null));
    }

    @Test
    public void testGetEstimatedSizeBytes() throws Exception {
        Assert.assertTrue(0 == inputSource.getEstimatedSizeBytes(null));
    }

    @Test
    public void testProducesSortedKeys() throws Exception {
        Assert.assertFalse(inputSource.producesSortedKeys(null));
    }
}