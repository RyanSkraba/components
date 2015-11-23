package org.talend.components.api.properties;

import org.talend.components.api.schema.SchemaElement;

/**
 * Make new {@link Property} objects.
 */
public class PropertyFactory {

    public static Property newProperty(String name) {
        return new Property(name);
    }

    public static Property newProperty(String name, String title) {
        return new Property(name, title);
    }

    public static Property newProperty(SchemaElement.Type type, String name, String title) {
        return new Property(type, name, title);
    }

    public static Property newProperty(SchemaElement.Type type, String name) {
        return new Property(type, name);
    }

    public static Property newString(String name) {
        return new Property(SchemaElement.Type.STRING, name);
    }

    public static Property newInteger(String name) {
        return new Property(SchemaElement.Type.INT, name);
    }

    public static Property newInteger(String name, String defaultValue) {
        Property property = new Property(name);
        property.setDefaultValue(defaultValue);
        return property;
    }

    public static Property newFloat(String name) {
        return new Property(SchemaElement.Type.FLOAT, name);
    }

    public static Property newBoolean(String name) {
        return new Property(SchemaElement.Type.BOOLEAN, name);
    }

    public static Property newDate(String name) {
        return new Property(SchemaElement.Type.DATE, name);
    }

    public static Property newEnum(String name) {
        return new Property(SchemaElement.Type.ENUM, name);
    }

    public static Property newEnum(String name, Object... values) {
        Property property = new Property(SchemaElement.Type.ENUM, name);
        property.setPossibleValues(values);
        return property;
    }

    /**
     * Used if there are returns to set the "returns" property with a {@link Property} that contains the returns
     * properties.
     *
     * @return a {@link Property} that will contain the return properties
     */
    public static Property setReturnsProperty() {
        // Container for the returns
        return new Property(ComponentProperties.RETURNS);
    }

    /**
     * Adds a new return property.
     *
     * @param returns the {@link Property} returned by {@link #setReturnsProperty()}
     * @param type the type of the returns property
     * @param name the name of the returns property
     * @return a {@link Property}
     */
    public static Property newReturnProperty(Property returns, SchemaElement.Type type, String name) {
        Property p = new Property(type, name);
        returns.addChild(p);
        return p;
    }

}
