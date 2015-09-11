package org.talend.components.api.schema;

/**
 * A schema that can be used by the component service
 */
public interface ComponentSchema {

    public ComponentSchemaElement getRoot();

    public void setRoot(ComponentSchemaElement root);

}
