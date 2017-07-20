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
package org.talend.components.test;

import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.junit.rules.TemporaryFolder;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;

/**
 * Reusable for creating a Beam {@link Pipeline} for running unit tests.
 */
public class BeamDirectTestResource extends TemporaryFolder {

    /** The current pipeline options for the test. */
    protected DirectOptions options = null;

    private BeamDirectTestResource() {

    }

    public static BeamDirectTestResource of() {
        return new BeamDirectTestResource();
    }

    /**
     * @return the options used to create this pipeline. These can be or changed before the Pipeline is created.
     */
    public DirectOptions getOptions() {
        if (options == null) {
            options = PipelineOptionsFactory.create().as(DirectOptions.class);
            options.setRunner(DirectRunner.class);
        }
        return options;
    }

    /**
     * @return a new pipeline created from the current state of {@link #getOptions()}.
     */
    public Pipeline createPipeline() {
        Pipeline p = Pipeline.create(getOptions());
        return p;
    }

    /**
     * @return a new pipeline created from the current state of {@link #getOptions()} and with the given target
     * parallelism.
     */
    public Pipeline createPipeline(int targetParallelism) {
        getOptions().setTargetParallelism(targetParallelism);
        Pipeline p = Pipeline.create(getOptions());
        return p;
    }

    /**
     * Clean up the Spark context after the test is run.
     */
    @Override
    protected void after() {
        if (options != null) {
            options = null;
        }
    }
}
