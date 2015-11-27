package org.talend.components.api.schema;

/**
 * A schema that can be used by the component service
 */
public interface Schema {

    public SchemaElement getRoot();

    public SchemaElement setRoot(SchemaElement root);

    /**
     * Returns a string that is the serialized form of this Schema. Use {@link SchemaFactory#fromSerialized(String)} to
     * materialize the object from the string.
     */
    public String toSerialized();

}
