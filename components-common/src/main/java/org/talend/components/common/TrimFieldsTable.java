package org.talend.components.common;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

public class TrimFieldsTable extends BasedOnSchemaTable {

    private static final TypeLiteral<List<Boolean>> LIST_BOOLEAN_TYPE = new TypeLiteral<List<Boolean>>() {// empty
    };

    public Property<List<Boolean>> trim = newProperty(LIST_BOOLEAN_TYPE, "trim");

    public TrimFieldsTable(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addColumn(trim);
    }

}
