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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import com.microsoft.azure.storage.table.EdmType;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.Operators;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;

public class FilterExpressionTable extends ComponentPropertiesImpl {

    private static final long serialVersionUID = -5175064100089239187L;

    public static final String ADD_QUOTES = "ADD_QUOTES";

    public static final String COMPARISON_EQUAL = "EQUAL";

    public static final String COMPARISON_NOT_EQUAL = "NOT EQUAL";

    public static final String COMPARISON_GREATER_THAN = "GREATER THAN";

    public static final String COMPARISON_GREATER_THAN_OR_EQUAL = "GREATER THAN OR EQUAL";

    public static final String COMPARISON_LESS_THAN = "LESS THAN";

    public static final String COMPARISON_LESS_THAN_OR_EQUAL = "LESS THAN OR EQUAL";

    public static final String PREDICATE_AND = "AND";

    public static final String PREDICATE_OR = "OR";

    public static final String PREDICATE_NOT = "NOT";

    public static final String FIELD_TYPE_STRING = "STRING";

    public static final String FIELD_TYPE_NUMERIC = "NUMERIC";

    public static final String FIELD_TYPE_DATE = "DATE";

    public static final String FIELD_TYPE_INT64 = "INT64";

    public static final String FIELD_TYPE_BINARY = "BINARY";

    public static final String FIELD_TYPE_GUID = "GUID";

    public static final String FIELD_TYPE_BOOLEAN = "BOOLEAN";

    public static final String[] COMPARISONS = { COMPARISON_EQUAL, COMPARISON_NOT_EQUAL, COMPARISON_GREATER_THAN,
            COMPARISON_GREATER_THAN_OR_EQUAL, COMPARISON_LESS_THAN, COMPARISON_LESS_THAN_OR_EQUAL };

    public static final String[] PREDICATES = { PREDICATE_AND, PREDICATE_OR, PREDICATE_NOT };

    public static final String[] FIELD_TYPES = { FIELD_TYPE_STRING, FIELD_TYPE_NUMERIC, FIELD_TYPE_DATE, FIELD_TYPE_INT64,
            FIELD_TYPE_BOOLEAN, FIELD_TYPE_BINARY, FIELD_TYPE_GUID };

    public static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    public Property<List<String>> column = newProperty(LIST_STRING_TYPE, "column"); // $NON-NLS-0$

    public Property<List<String>> operand = newProperty(LIST_STRING_TYPE, "operand"); //$NON-NLS-1$

    public Property<List<String>> function = newProperty(LIST_STRING_TYPE, "function");

    public Property<List<String>> predicate = newProperty(LIST_STRING_TYPE, "predicate");

    public Property<List<String>> fieldType = newProperty(LIST_STRING_TYPE, "fieldType");

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(FilterExpressionTable.class);

    public FilterExpressionTable(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        column.setTaggedValue(ADD_QUOTES, true);
        operand.setTaggedValue(ADD_QUOTES, true);

        column.setPossibleValues(Arrays.asList("PartitionKey", "RowKey", "Timestamp"));
        operand.setPossibleValues(Arrays.asList("US Customers", "UKey", "2018-01-01"));
        function.setPossibleValues(COMPARISONS);
        predicate.setPossibleValues(PREDICATES);
        fieldType.setPossibleValues(FIELD_TYPES);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(column);
        mainForm.addColumn(Widget.widget(function).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(operand);
        mainForm.addColumn(Widget.widget(predicate).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(fieldType).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
    }

    public String getComparison(String f) {
        switch (f) {
        case COMPARISON_EQUAL:
            return QueryComparisons.EQUAL;
        case COMPARISON_NOT_EQUAL:
            return QueryComparisons.NOT_EQUAL;
        case COMPARISON_GREATER_THAN:
            return QueryComparisons.GREATER_THAN;
        case COMPARISON_GREATER_THAN_OR_EQUAL:
            return QueryComparisons.GREATER_THAN_OR_EQUAL;
        case COMPARISON_LESS_THAN:
            return QueryComparisons.LESS_THAN;
        case COMPARISON_LESS_THAN_OR_EQUAL:
            return QueryComparisons.LESS_THAN_OR_EQUAL;
        default:
            return null;
        }
    }

    public String getOperator(String p) {
        switch (p) {
        case PREDICATE_AND:
            return Operators.AND;
        case PREDICATE_OR:
            return Operators.OR;
        case PREDICATE_NOT:
            return Operators.NOT;
        default:
            return null;
        }
    }

    public EdmType getType(String ft) {
        switch (ft) {
        case FIELD_TYPE_STRING:
            return EdmType.STRING;
        case FIELD_TYPE_NUMERIC:
            return EdmType.INT32;
        case FIELD_TYPE_INT64:
            return EdmType.INT64;
        case FIELD_TYPE_DATE:
            return EdmType.DATE_TIME;
        case FIELD_TYPE_BINARY:
            return EdmType.BINARY;
        case FIELD_TYPE_GUID:
            return EdmType.GUID;
        case FIELD_TYPE_BOOLEAN:
            return EdmType.BOOLEAN;
        default:
            return null;
        }
    }

    public int size() {
        if (column.getValue() != null && !column.getValue().isEmpty())
            return column.getValue().size();
        else
            return 0;
    }

    public ValidationResult validateFilterExpession() {
        Boolean filterOk = true;
        if (size() == 0)
            return ValidationResult.OK;
        for (int idx = 0; idx < column.getValue().size(); idx++) {
            filterOk = filterOk && (!column.getValue().get(idx).isEmpty()) && (!operand.getValue().get(idx).isEmpty());
            if (!filterOk) {
                ValidationResult vr = new ValidationResult();
                vr.setStatus(Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.MissName"));
                return vr;
            }
        }

        return ValidationResult.OK;
    }

    public String getCombinedFilterConditions() {
        String filter = "";
        if (validateFilterExpession().getStatus().equals(Result.ERROR))
            return filter;
        for (int idx = 0; idx < size(); idx++) {
            String c = column.getValue().get(idx);
            String cfn = function.getValue().get(idx);
            String cop = predicate.getValue().get(idx);
            String typ = fieldType.getValue().get(idx);

            String f = getComparison(cfn);
            String v = operand.getValue().get(idx);
            String p = getOperator(cop);

            EdmType t = getType(typ);

            String flt = TableQuery.generateFilterCondition(c, f, v, t);

            if (!filter.isEmpty()) {
                filter = TableQuery.combineFilters(filter, p, flt);
            } else {
                filter = flt;
            }
        }
        return filter;
    }

}
