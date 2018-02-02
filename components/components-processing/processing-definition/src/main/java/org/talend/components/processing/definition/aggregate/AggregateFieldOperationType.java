package org.talend.components.processing.definition.aggregate;

public enum AggregateFieldOperationType {
    // Add all elements in one group into a list, the order will mess, consider null value as well
    // the output type will be List<InputType>
    LIST,
    // Count the elements in one group, consider null value as well.
    // the output type will be Long
    COUNT,
    // Sum all the number type(int/long/float/double) elements in one group
    // the output type will be Long/Double
    SUM,
    // Calculate average of the elements in one group, count null as denominator
    // the output type will be Double
    AVG,
    // Output the min value of the elements in one group
    // the output type will be the same of input type
    MIN,
    // Output the max value of the elements in one group
    // the output type will be the same of input type
    MAX
    // For Sum/Avg/Min/Max if all the incoming value is null, the output will be null
}
