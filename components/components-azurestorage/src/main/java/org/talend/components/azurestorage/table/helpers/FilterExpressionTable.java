// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azurestorage.table.helpers;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import com.microsoft.azure.storage.table.EdmType;
import com.microsoft.azure.storage.table.TableQuery;

public class FilterExpressionTable extends ComponentPropertiesImpl {

    private static final long serialVersionUID = 7449848799595979627L;

    public static final String ADD_QUOTES = "ADD_QUOTES";

    public static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    public Property<List<String>> column = newProperty(LIST_STRING_TYPE, "column"); // $NON-NLS-0$

    public Property<List<String>> operand = newProperty(LIST_STRING_TYPE, "operand"); //$NON-NLS-1$

    public Property<List<String>> function = newProperty(LIST_STRING_TYPE, "function");

    public Property<List<String>> predicate = newProperty(LIST_STRING_TYPE, "predicate");

    public Property<List<String>> fieldType = newProperty(LIST_STRING_TYPE, "fieldType");

    private final List<String> schemaColumnNames = new ArrayList<>();

    public FilterExpressionTable(String name) {
        super(name);
        updateColumnsNames();
    }

    public void updateSchemaColumnNames(List<String> columnNames) {
        this.schemaColumnNames.clear();
        if (columnNames != null) {
            this.schemaColumnNames.addAll(columnNames);
            updateColumnsNames();
        }
    }

    private void updateColumnsNames() {
        column.setPossibleValues(schemaColumnNames);
        if (schemaColumnNames.isEmpty()) {
            column.setValue(null);
            function.setValue(null);
            operand.setValue(null);
            predicate.setValue(null);
            fieldType.setValue(null);
        }
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        operand.setTaggedValue(ADD_QUOTES, true);
        function.setPossibleValues(Comparison.possibleValues());
        predicate.setPossibleValues(Predicate.possibleValues());
        fieldType.setPossibleValues(SupportedFieldType.possibleValues());

        updateColumnsNames();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(Widget.widget(column).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(function).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(operand);
        mainForm.addColumn(Widget.widget(predicate).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(fieldType).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
    }

    /**
     * this method check if the data in the Filter expression is valid and can produce a Query filter.<br/>
     * the table is valid if :<br>
     * 1) all column, fieldType, function, operand and predicate lists are not null<br/>
     * 2) values in the lists column, fieldType, function, operand and predicate are not empty
     * 
     * <br/>
     * 
     * @return {@code true } if the two above condition are true
     * 
     */
    private boolean isValidFilterExpession() {

        if (column.getValue() == null || fieldType.getValue() == null || function.getValue() == null || operand.getValue() == null
                || predicate.getValue() == null) {

            return false;
        }

        int tableSize = column.getValue().size();
        for (int i = 0; i < tableSize; i++) {
            if (StringUtils.isEmpty(column.getValue().get(i)) || StringUtils.isEmpty(fieldType.getValue().get(i))
                    || StringUtils.isEmpty(function.getValue().get(i)) || StringUtils.isEmpty(operand.getValue().get(i))
                    || StringUtils.isEmpty(predicate.getValue().get(i))) {

                return false;
            }
        }

        return true;
    }

    public String generateCombinedFilterConditions() {
        String filter = "";
        if (isValidFilterExpession()) {
            for (int idx = 0; idx < column.getValue().size(); idx++) {
                String c = column.getValue().get(idx);
                String cfn = function.getValue().get(idx);
                String cop = predicate.getValue().get(idx);
                String typ = fieldType.getValue().get(idx);

                String f = Comparison.getQueryComparisons(cfn);
                String v = operand.getValue().get(idx);
                String p = Predicate.getOperator(cop);

                EdmType t = SupportedFieldType.getEdmType(typ);

                String flt = TableQuery.generateFilterCondition(c, f, v, t);

                if (!filter.isEmpty()) {
                    filter = TableQuery.combineFilters(filter, p, flt);
                } else {
                    filter = flt;
                }
            }
        }
        return filter;
    }

}
