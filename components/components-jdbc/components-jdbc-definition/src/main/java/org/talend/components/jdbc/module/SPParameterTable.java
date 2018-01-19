package org.talend.components.jdbc.module;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class SPParameterTable extends ComponentPropertiesImpl {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    public SPParameterTable(String name) {
        super(name);
    }

    public Property<List<String>> schemaColumns = newProperty(LIST_STRING_TYPE, "schemaColumns");

    public Property<List<String>> parameterTypes = newProperty(LIST_STRING_TYPE, "parameterTypes");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = CommonUtils.addForm(this, Form.MAIN);
        mainForm.addColumn(Widget.widget(schemaColumns).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(parameterTypes).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        List<String> values = new ArrayList<>();
        for (ParameterType value : ParameterType.values()) {
            values.add(value.toString());
        }
        parameterTypes.setPossibleValues(values);

        schemaColumns.setTaggedValue(ComponentConstants.ADD_QUOTES, true);
    }

    public enum ParameterType {
        IN,
        OUT,
        INOUT,
        RECORDSET
    }
}
