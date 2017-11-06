package org.talend.components.processing.definition.typeconverter;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;

public class TypeConverterPropertiesTest {

    @Test
    public void testDefaultProperties() {
        TypeConverterProperties properties = new TypeConverterProperties("test");
        assertThat(properties.converters.getDefaultProperties(), notNullValue());
        assertThat(properties.getAllSchemaPropertiesConnectors(true), contains(properties.OUTGOING_CONNECTOR));
        assertThat(properties.getAllSchemaPropertiesConnectors(false), contains(properties.INCOMING_CONNECTOR));
    }
}
