package org.talend.components.api.schema;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.internal.Property;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.api.schema.internal.SchemaImpl;

/**
 * Make objects that are related to the component schema.
 */
public class SchemaFactory {

    public static Schema newSchema() {
        return new SchemaImpl();
    }

    public static SchemaElement newProperty(String name) {
        return new Property(name);
    }

    public static SchemaElement newProperty(String name, String title) {
        return new Property(name, title);
    }

    public static SchemaElement newProperty(SchemaElement.Type type, String name, String title) {
        return new Property(type, name, title);
    }

    public static SchemaElement newProperty(SchemaElement.Type type, String name) {
        return new Property(type, name);
    }

    /**
     * Used if there are returns to set the "returns" property with a {@link SchemaElement} that contains the returns
     * properties.
     * 
     * @return a {@link SchemaElement} that will contain the return properties
     */
    public static SchemaElement setReturnsProperty() {
        // Container for the returns
        return new Property(ComponentProperties.RETURNS);
    }

    /**
     * Adds a new return property.
     * 
     * @param returns the {@link SchemaElement} returned by {@link #setReturnsProperty()}
     * @param type the type of the returns property
     * @param name the name of the returns property
     * @return a {@link SchemaElement}
     */
    public static SchemaElement newReturnProperty(SchemaElement returns, SchemaElement.Type type, String name) {
        Property p = new Property(type, name);
        returns.addChild(p);
        return p;
    }


    public static SchemaElement newSchemaElement(SchemaElement.Type type, String name) {
        SchemaElement se = new DataSchemaElement();
        se.setName(name);
        se.setType(type);
        return se;
    }


}
