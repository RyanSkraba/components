package org.talend.components.common;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

public class BasedOnSchemaTable extends ComponentPropertiesImpl {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {// empty
    };

    public static final String ADD_QUOTES = "ADD_QUOTES";

    public Property<List<String>> columnName = newProperty(LIST_STRING_TYPE, "columnName");

    public BasedOnSchemaTable(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(columnName);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        columnName.setTaggedValue(ADD_QUOTES, true);
    }
}
