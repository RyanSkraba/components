package org.talend.components.processing.definition.filterrow;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LogicalOpTypeTest {

    public static int count = 0;

    /**
     * Apply the pattern for aggregating a list of booleans using the logical operation. This also counts the number of
     * values that were aggregated before the operation could be short-circuited.
     * 
     * @param op The operation to apply.
     * @param valuesToAggregate The list of values to aggregate.
     * @return Whether or not the list of booleans aggregated with the operator returns true.
     */
    public static boolean combine(LogicalOpType op, boolean... valuesToAggregate) {
        // Start with an aggregate state that describes the operation before any value
        // is applied.
        boolean aggregate = op.createAggregate();

        count = 0;
        for (boolean nextValue : valuesToAggregate) {
            // Add each consecutive value to the aggregate state.
            aggregate = op.combineAggregate(aggregate, nextValue);
            count++;
            // Optional: if the operation can be short-circuited based on the current state.
            if (op.canShortCircuit(aggregate))
                break;
        }

        return aggregate;
    }

    /**
     * When you aggregate a list of no values.
     */
    @Test
    public void testAggregateNothing() {
        assertThat(combine(LogicalOpType.ALL), is(true));
        assertThat(count, is(0));
        assertThat(combine(LogicalOpType.ANY), is(false));
        assertThat(count, is(0));
        assertThat(combine(LogicalOpType.NONE), is(true));
        assertThat(count, is(0));
    }

    @Test
    public void testAll() {
        // You have to look at all of the values to ensure they're all true.
        assertThat(combine(LogicalOpType.ALL, true), is(true));
        assertThat(count, is(1));
        assertThat(combine(LogicalOpType.ALL, true, true), is(true));
        assertThat(count, is(2));
        assertThat(combine(LogicalOpType.ALL, true, true, true), is(true));
        assertThat(count, is(3));

        // But you can stop looking at the first false.
        assertThat(combine(LogicalOpType.ALL, false, true, true), is(false));
        assertThat(count, is(1));
        assertThat(combine(LogicalOpType.ALL, true, false, true), is(false));
        assertThat(count, is(2));
        assertThat(combine(LogicalOpType.ALL, true, true, false), is(false));
        assertThat(count, is(3));
    }

    @Test
    public void testAny() {
        // You have to look at all of the values to ensure they're all false.
        assertThat(combine(LogicalOpType.ANY, false), is(false));
        assertThat(count, is(1));
        assertThat(combine(LogicalOpType.ANY, false, false), is(false));
        assertThat(count, is(2));
        assertThat(combine(LogicalOpType.ANY, false, false, false), is(false));
        assertThat(count, is(3));

        // But you can stop looking at the first true.
        assertThat(combine(LogicalOpType.ANY, true, false, false), is(true));
        assertThat(count, is(1));
        assertThat(combine(LogicalOpType.ANY, false, true, false), is(true));
        assertThat(count, is(2));
        assertThat(combine(LogicalOpType.ANY, false, false, true), is(true));
        assertThat(count, is(3));
    }

    @Test
    public void testNone() {
        // You have to look at all of the values to ensure they're all false.
        assertThat(combine(LogicalOpType.NONE, false), is(true));
        assertThat(count, is(1));
        assertThat(combine(LogicalOpType.NONE, false, false), is(true));
        assertThat(count, is(2));
        assertThat(combine(LogicalOpType.NONE, false, false, false), is(true));
        assertThat(count, is(3));

        // But you can stop looking at the first true.
        assertThat(combine(LogicalOpType.NONE, true, false, false), is(false));
        assertThat(count, is(1));
        assertThat(combine(LogicalOpType.NONE, false, true, false), is(false));
        assertThat(count, is(2));
        assertThat(combine(LogicalOpType.NONE, false, false, true), is(false));
        assertThat(count, is(3));
    }

}