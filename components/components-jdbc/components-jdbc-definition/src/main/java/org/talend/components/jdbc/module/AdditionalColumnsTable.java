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

public class AdditionalColumnsTable extends ComponentPropertiesImpl {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    private static final TypeLiteral<List<Position>> LIST_POSITION_TYPE = new TypeLiteral<List<Position>>() {
    };

    public AdditionalColumnsTable(String name) {
        super(name);
    }

    public Property<List<String>> names = newProperty(LIST_STRING_TYPE, "names");

    public Property<List<String>> sqlExpressions = newProperty(LIST_STRING_TYPE, "sqlExpressions");

    public Property<List<String>> positions = newProperty(LIST_STRING_TYPE, "positions");

    public Property<List<String>> referenceColumns = newProperty(LIST_STRING_TYPE, "referenceColumns");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = CommonUtils.addForm(this, Form.MAIN);
        mainForm.addColumn(names);
        mainForm.addColumn(sqlExpressions);
        mainForm.addColumn(Widget.widget(positions).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(referenceColumns).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
    }
    
    @Override
    public void setupProperties() {
        super.setupProperties();
        
        List<String> values = new ArrayList<>();
        for (Position value : Position.values()) {
            values.add(value.toString());
        }
        positions.setPossibleValues(values);
        
        referenceColumns.setTaggedValue(ComponentConstants.ADD_QUOTES, true);
    }

    public enum Position {
        BEFORE,
        AFTER,
        REPLACE
    }
}
