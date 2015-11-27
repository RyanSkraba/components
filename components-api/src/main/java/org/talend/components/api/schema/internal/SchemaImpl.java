package org.talend.components.api.schema.internal;

import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;

import com.cedarsoftware.util.io.JsonWriter;

public class SchemaImpl implements Schema {

    protected SchemaElement root;

    @Override
    public SchemaElement getRoot() {
        return root;
    }

    @Override
    public SchemaElement setRoot(SchemaElement root) {
        this.root = root;
        return root;
    }

    @Override
    public String toSerialized() {
        return JsonWriter.objectToJson(this);
    }
}
