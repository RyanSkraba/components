package org.talend.components.api.schema;

/**
 * A schema that can be used by the component service
 */
public interface Schema {

    public SchemaElement getRoot();

    public void setRoot(SchemaElement root);

}
