package org.talend.components.jdbc;

import org.apache.avro.Schema;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;

public class CommonUtils {

    public static Schema getSchema(SchemaProperties schema) {
        return (Schema) schema.schema.getValue();
    }

    public static Form addForm(Properties props, String formName) {
        return new Form(props, formName);
    }
}
