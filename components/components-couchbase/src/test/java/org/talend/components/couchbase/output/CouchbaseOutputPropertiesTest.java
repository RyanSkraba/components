package org.talend.components.couchbase.output;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.properties.presentation.Form;

public class CouchbaseOutputPropertiesTest {

    private CouchbaseOutputProperties properties;

    @Before
    public void setup() {
        properties = new CouchbaseOutputProperties("output properties");
    }

    @Test
    public void testSetupProperties() {
        Assert.assertNull(properties.idFieldName.getValue());

        properties.setupProperties();

        Assert.assertNotNull(properties.idFieldName.getValue());
    }

    @Test
    public void testSetupLayout() {
        Assert.assertNull(properties.getForm(Form.MAIN));

        properties.schema.setupLayout();
        properties.setupLayout();

        Form mainForm = properties.getForm(Form.MAIN);
        Assert.assertNotNull(mainForm);
        Assert.assertNotNull(mainForm.getWidget(properties.idFieldName));
    }

    @Test
    public void testOutputConnectorGetAllSchemaPropertiesConnectors() {
        Assert.assertTrue(properties.getAllSchemaPropertiesConnectors(true).isEmpty());
    }

    @Test
    public void testInputConnectorGetAllSchemaPropertiesConnectors() {
        Set<PropertyPathConnector> propertyPathConnectors = properties.getAllSchemaPropertiesConnectors(false);
        Assert.assertEquals(1, propertyPathConnectors.size());
    }
}
