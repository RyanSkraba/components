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
package org.talend.components.adapter.beam.transform;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.talend.daikon.java8.Consumer;

/**
 * Unit tests for {@link DirectConsumerCollector}.
 */
public class DirectConsumerCollectorTest {
    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    /**
     * Demonstrates the basic use case for the {@link DirectConsumerCollector}.
     */
    @Test
    public void testBasic() {

        // This might fail if another test didn't clean up properly.
        assertThat(DirectConsumerCollector.getUids(), Matchers.<String> emptyIterable());

        Consume<String> beforeConsumer = new Consume<>();
        Consume<Integer> afterConsumer = new Consume<>();

        // Creating the collectors in the try-with-resources ensures that they are automatically cleaned up. Otherwise,
        // the close() method on each instance should be called after the test.
        try (DirectConsumerCollector<String> before = DirectConsumerCollector.of(beforeConsumer); //
                DirectConsumerCollector<Integer> after = DirectConsumerCollector.of(afterConsumer);) {

            PCollection<String> input = pipeline.apply( //
                    Create.of("one", "two", "three")); //

            // Collect the results before and after the transformation.
            input.apply(before);
            PCollection<Integer> output = input.apply("tx", ParDo.of(new DirectCollectorTest.GetLength()));
            output.apply(after);

            // Optional -- validate the contents of the collections in the pipeline.
            PAssert.that(input).containsInAnyOrder("one", "two", "three");
            PAssert.that(output).containsInAnyOrder(3, 3, 5);

            // Run the pipeline to fill the collectors.
            pipeline.run().waitUntilFinish();

            assertThat(DirectConsumerCollector.getUids(), Matchers.<String> iterableWithSize(2));
        }

        // Validate the contents of the collected outputs.
        // assertThat(beforeConsumer.consumed, hasSize(3));
        assertThat(beforeConsumer.consumed, containsInAnyOrder("one", "two", "three"));
        // assertThat(afterConsumer.consumed, hasSize(3));
        assertThat(afterConsumer.consumed, containsInAnyOrder(3, 3, 5));

        // Ensure that the collectors were cleaned up.
        assertThat(DirectConsumerCollector.getUids(), Matchers.<String> emptyIterable());
    }

    /** Converts a String to its length. */
    public static class Consume<T> implements Consumer<T> {

        public final List<T> consumed = new ArrayList<>();

        @Override
        public synchronized void accept(T t) {
            consumed.add(t);
        }
    }

}
