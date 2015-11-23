package org.talend.components.api.schema;

import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.api.schema.internal.SchemaImpl;

/**
 * Make objects that are related to the schemas.
 */
public class SchemaFactory {

    public static Schema newSchema() {
        return new SchemaImpl();
    }

    public static SchemaElement newSchemaElement(SchemaElement.Type type, String name) {
        SchemaElement se = newSchemaElement(name);
        se.setType(type);
        return se;
    }

    public static SchemaElement newSchemaElement(String name) {
        SchemaElement se = new DataSchemaElement();
        se.setName(name);
        return se;
    }

}
