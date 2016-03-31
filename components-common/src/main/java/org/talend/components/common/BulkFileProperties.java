package org.talend.components.common;

import org.apache.avro.Schema;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.HasSchemaProperty;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

import java.util.Arrays;
import java.util.List;

import static org.talend.daikon.properties.PropertyFactory.newProperty;

public class BulkFileProperties extends ComponentProperties implements HasSchemaProperty {

    public Property bulkFilePath = newProperty("bulkFilePath").setRequired();

    public Property append = newProperty(Property.Type.BOOLEAN, "append");

    public SchemaProperties schema = new SchemaProperties("schema");

    public BulkFileProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        mainForm.addRow(bulkFilePath);
        mainForm.addRow(append);

    }
    @Override
    public List<Schema> getSchemas() {
        return Arrays.asList(new Schema[]{new Schema.Parser().parse(schema.schema.getStringValue())});
    }

    @Override
    public void setSchemas(List<Schema> schemas) {
        schema.schema.setValue(schemas.get(0));
    }
}
