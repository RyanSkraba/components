package org.talend.components.api.schema.internal;

import org.talend.components.api.schema.AbstractSchemaElement;
import org.talend.components.api.schema.SchemaElement;

/**
 * This implementation shall be used to represent meta data elements This typically define DisplayName to be the same
 * value as Name because the technical name of the metadata schema is never translated
 */

public class DataSchemaElement extends AbstractSchemaElement {

    /**
     * the displayName is returning the current name because for real data schema the display name never gets
     * translated.
     */
    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public SchemaElement setDisplayName(String name) {
        setName(name);
        return this;
    }

}
