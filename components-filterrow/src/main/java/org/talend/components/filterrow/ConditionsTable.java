// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.OperatorType;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class ConditionsTable extends ComponentPropertiesImpl {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {// empty
    };

    public static final String ADD_QUOTES = "ADD_QUOTES";

    private static final TypeLiteral<List<FunctionType>> LIST_FUNCTION_TYPE = new TypeLiteral<List<FunctionType>>() {// empty
    };

    private static final TypeLiteral<List<OperatorType>> LIST_OPERATORS_TYPE = new TypeLiteral<List<OperatorType>>() {// empty
    };

    private static final TypeLiteral<List<Object>> LIST_OBJECT_TYPE = new TypeLiteral<List<Object>>() {// empty
    };

    public Property<List<String>> columnName = PropertyFactory.newProperty(LIST_STRING_TYPE, "columnName");

    public Property<List<FunctionType>> function = PropertyFactory.newProperty(LIST_FUNCTION_TYPE, "function");

    public Property<List<OperatorType>> operator = PropertyFactory.newProperty(LIST_OPERATORS_TYPE, "operator");

    public Property<List<Object>> value = PropertyFactory.newProperty(LIST_OBJECT_TYPE, "value");

    private final List<String> schemaColumnNames = new LinkedList<>();

    public ConditionsTable(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addColumn(Widget.widget(columnName).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(function).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(operator).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(value);
    }

    public void updateSchemaColumnNames(List<String> columnNames) {
        this.schemaColumnNames.clear();
        if (columnNames != null) {
            this.schemaColumnNames.addAll(columnNames);
            updateColumnsNames();
        }
    }

    private void updateColumnsNames() {
        columnName.setPossibleValues(schemaColumnNames);
        if (schemaColumnNames.size() == 0) {
            columnName.setValue(null);
            function.setValue(null);
            operator.setValue(null);
            value.setValue(null);
        }
    }

    public boolean isEditable() {
        return schemaColumnNames.size() > 0;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        function.setPossibleValues(FunctionType.values());
        operator.setPossibleValues(OperatorType.values());
        columnName.setTaggedValue(ADD_QUOTES, true);
        value.setTaggedValue(ADD_QUOTES, false);
        operator.setTaggedValue(ADD_QUOTES, false);
        updateColumnsNames();
    }

}
