package org.talend.components.common;

import static org.talend.daikon.properties.PropertyFactory.*;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

public class BulkFileProperties extends FixedConnectorsComponentProperties {

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
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        return Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "schema"));
    }
}
