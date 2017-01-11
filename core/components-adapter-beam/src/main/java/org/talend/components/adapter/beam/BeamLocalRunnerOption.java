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
package org.talend.components.adapter.beam;

import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeamLocalRunnerOption {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeamLocalRunnerOption.class);
    private static DirectOptions options = null;
    
    public static DirectOptions getOptions() {
        if (options == null) {
            LOGGER.info("Create DirectOption");
            options = PipelineOptionsFactory.as(DirectOptions.class);
            options.setTargetParallelism(1);
            options.setRunner(DirectRunner.class);
            options.setEnforceEncodability(false);
            options.setEnforceImmutability(false);
        }
        return options;
    }
}
