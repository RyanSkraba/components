package org.talend.components.api.properties;

import org.apache.avro.Schema;

import java.util.List;

public interface HasSchemaProperty {

    public List<Schema> getSchemas();

    public void setSchemas(List<Schema> schemas);
}
