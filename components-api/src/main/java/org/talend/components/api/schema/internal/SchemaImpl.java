package org.talend.components.api.schema.internal;

import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;

public class SchemaImpl implements Schema {

    protected SchemaElement root;

    @Override
    public SchemaElement getRoot() {
        return root;
    }

    @Override
    public void setRoot(SchemaElement root) {
        this.root = root;
    }

}
