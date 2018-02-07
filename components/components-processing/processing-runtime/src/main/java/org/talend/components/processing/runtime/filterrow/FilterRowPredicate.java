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
package org.talend.components.processing.runtime.filterrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.processing.definition.ProcessingErrorCode;
import org.talend.components.processing.definition.filterrow.ConditionsRowConstant;
import org.talend.components.processing.definition.filterrow.FilterRowCriteriaProperties;
import org.talend.components.processing.definition.filterrow.FilterRowProperties;
import org.talend.components.processing.definition.filterrow.LogicalOpType;

import scala.collection.JavaConversions;
import scala.util.Try;
import wandou.avpath.Evaluator;

/**
 * A function that takes an input IndexedRecord and returns whether it matches the given filter criteria.
 */
public class FilterRowPredicate implements SerializableFunction<IndexedRecord, Boolean> {

    private final FilterRowProperties properties;

    public FilterRowPredicate(FilterRowProperties properties) {
        this.properties = properties;
        // Remove any filter criteria that have not been initialized by the user. These elements are ignored.
        List<FilterRowCriteriaProperties> filters = this.properties.filters.subProperties;
        this.properties.filters.subProperties = new ArrayList<>();
        for (FilterRowCriteriaProperties criteria : filters) {
            if (!StringUtils.isEmpty(criteria.columnName.getStringValue())
                    || !StringUtils.isEmpty(criteria.value.getStringValue())) {
                this.properties.filters.subProperties.add(criteria);
            }
        }
    }

    @Override
    public Boolean apply(IndexedRecord input) {
        // Short-circuit when the user hasn't filled out any filters.
        if (properties.filters.subProperties.size() == 0)
            return true;

        // This is the logical operation applied to the set of filter criteria in this component.
        // (i.e. ALL means that all criteria must evaluate to true.)
        LogicalOpType criteriaLogicalOp = properties.logicalOp.getValue();

        // Starting point for aggregating the logical operations.
        boolean aggregate = criteriaLogicalOp.createAggregate();

        // Apply all of the criteria.
        for (FilterRowCriteriaProperties criteria : properties.filters.subProperties) {
            aggregate = criteriaLogicalOp.combineAggregate(aggregate, evaluateCriteria(criteria, input));
            if (criteriaLogicalOp.canShortCircuit(aggregate))
                break;
        }

        return aggregate;
    }

    /**
     * Evaluate one specific criteria against the given indexed record.
     *
     * @param criteria the criteria to evaluate.
     * @param record the value to evaluate against the criteria.
     * @return whether the record should be selected for this specific criteria.
     */
    private boolean evaluateCriteria(FilterRowCriteriaProperties criteria, IndexedRecord record) {
        // This is the logical operation applied to multiple values applied inside ONE specific filter criteria.
        // When using a complex av expression, one accessor can read multiple values.
        // (i.e. ALL means that all values must evaluate to true.)
        LogicalOpType fieldOp = LogicalOpType.ALL;

        // Starting point for aggregating the logical operations.
        boolean aggregate = fieldOp.createAggregate();

        String accessor = criteria.columnName.getStringValue();
        if (StringUtils.isEmpty(accessor)) {
            return false;
        }

        List<Object> values = getInputFields(record, accessor);

        if (ConditionsRowConstant.Function.COUNT.equals(criteria.function.getStringValue())) {
            values = Arrays.asList((Object) values.size());
        } else if (values.size() == 0) {
            // If the function is not COUNT and no values are returned, then consider the criteria not matched.
            return false;
        }

        // Apply all of the criteria.
        for (Object value : values) {
            aggregate = fieldOp.combineAggregate(aggregate, checkCondition(value, criteria));
            if (fieldOp.canShortCircuit(aggregate))
                break;
        }

        return aggregate;
    }

    private <T extends Comparable<T>> Boolean checkCondition(Object inputValue, FilterRowCriteriaProperties filter) {
        String function = filter.function.getValue();
        String conditionOperator = filter.operator.getValue();
        String referenceValue = filter.value.getValue();

        // Apply the transformation function on the input value
        inputValue = FilterRowUtils.applyFunction(inputValue, function);

        if (referenceValue != null) {
            // TODO: do not cast the reference value at each comparison
            Class<T> inputValueClass = TypeConverterUtils.getComparableClass(inputValue);
            if (inputValueClass != null) {
                T convertedReferenceValue = TypeConverterUtils.parseTo(referenceValue, inputValueClass);
                return FilterRowUtils.compare(inputValueClass.cast(inputValue), conditionOperator, convertedReferenceValue);
            } else {
                return FilterRowUtils.compare(inputValue.toString(), conditionOperator, referenceValue.toString());
            }
        } else {
            if (ConditionsRowConstant.Operator.EQUAL.equals(conditionOperator)) {
                return inputValue == null;
            } else { // Not Equals
                return inputValue != null;
            }
        }
    }

    private List<Object> getInputFields(IndexedRecord inputRecord, String columnName) {
        // Adapt non-avpath syntax to avpath.
        // TODO: This should probably not be automatic, use the actual syntax.
        if (!columnName.startsWith("."))
            columnName = "." + columnName;
        Try<scala.collection.immutable.List<Evaluator.Ctx>> result = wandou.avpath.package$.MODULE$.select(inputRecord,
                columnName);
        List<Object> values = new ArrayList<Object>();
        if (result.isSuccess()) {
            for (Evaluator.Ctx ctx : JavaConversions.asJavaCollection(result.get())) {
                values.add(ctx.value());
            }
        } else {
            // Evaluating the expression failed, and we can handle the exception.
            Throwable t = result.failed().get();
            throw ProcessingErrorCode.createAvpathSyntaxError(t, columnName, -1);
        }
        return values;
    }

    /**
     * A function that returns the exact same result as NOT FilterRowPredicate.
     */
    public static class Negate extends FilterRowPredicate {

        public Negate(FilterRowProperties properties) {
            super(properties);
        }

        @Override
        public Boolean apply(IndexedRecord input) {
            return !super.apply(input);
        }
    }
}
