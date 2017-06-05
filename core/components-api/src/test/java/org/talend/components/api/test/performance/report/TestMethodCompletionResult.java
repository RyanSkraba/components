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
package org.talend.components.api.test.performance.report;

import org.databene.contiperf.PerformanceRequirement;
import org.databene.stat.LatencyCounter;

/**
 * created by dmytro.chmyga on Mar 31, 2017
 */
public class TestMethodCompletionResult {

    private final TestMethodReportDefinition definition;

    private final LatencyCounter mainCounter;

    private final PerformanceRequirement perfRequirement;

    public TestMethodCompletionResult(TestMethodReportDefinition definition, LatencyCounter mainCounter,
            PerformanceRequirement perfRequirement) {
        this.definition = definition;
        this.mainCounter = mainCounter;
        this.perfRequirement = perfRequirement;
    }

    public TestMethodReportDefinition getDefinition() {
        return definition;
    }

    public LatencyCounter getMainCounter() {
        return mainCounter;
    }

    public PerformanceRequirement getPerfRequirement() {
        return perfRequirement;
    }

}
