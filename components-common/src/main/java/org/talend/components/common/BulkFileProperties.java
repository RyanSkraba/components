package org.talend.components.common;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

import static org.talend.daikon.properties.PropertyFactory.newProperty;

/**
 *
 */
public class BulkFileProperties extends ComponentProperties {

    public Property bulkFilePath = newProperty("bulkFilePath");

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
}
