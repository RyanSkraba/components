package org.talend.components.api.properties;

import org.apache.avro.Schema;

import java.util.List;

public interface XHasSchemaProperty {

    public List<Schema> getSchemas();

    public void setSchemas(List<Schema> schemas);
}
