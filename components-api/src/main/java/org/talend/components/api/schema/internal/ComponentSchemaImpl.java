package org.talend.components.api.schema.internal;

import org.talend.components.api.schema.ComponentSchema;
import org.talend.components.api.schema.ComponentSchemaElement;

public class ComponentSchemaImpl implements ComponentSchema {

    protected ComponentSchemaElement root;

    @Override
    public ComponentSchemaElement getRoot() {
        return root;
    }

    @Override
    public void setRoot(ComponentSchemaElement root) {
        this.root = root;
    }
}
