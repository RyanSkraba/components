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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.databene.contiperf.PerformanceRequirement;
import org.databene.stat.LatencyCounter;

/**
 * Performance Tests results holder - class provided to hold the results of Performance tests results.
 */
public class PerformanceTestsResultHolder {

    private final Map<String, List<TestResult>> testResultsByName = new HashMap<>();

    private final Map<String, TestMethodCompletionResult> completionResults = Collections
            .synchronizedMap(new HashMap<String, TestMethodCompletionResult>());

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Add test result for test method.
     * 
     * @param serviceId full method name including test class name
     * @param latency time taken for test method run
     * @param startTime timestamp of test method invocation start
     */
    public void addResult(String serviceId, int latency, long startTime) {
        List<TestResult> results = null;
        try {
            lock.lock();
            results = testResultsByName.get(serviceId);
            if (results == null) {
                results = new ArrayList<>();
                testResultsByName.put(serviceId, results);
            }
            results.add(new TestResult(latency, startTime));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get and remove test results by full method name
     * 
     * @param serviceId full method name including test class name
     */
    public List<TestResult> pollTestResults(String serviceId) {
        try {
            lock.lock();
            return testResultsByName.remove(serviceId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add result of all test method invocations if there were more than one
     * 
     * @param definition test method definition
     * @param mainCounter latency counter for the test method
     * @param perfRequirement Performance requirement for given test method
     */
    public void addCompletionResult(TestMethodReportDefinition definition, LatencyCounter mainCounter,
            PerformanceRequirement perfRequirement) {
        completionResults.put(definition.getFullMethodName(),
                new TestMethodCompletionResult(definition, mainCounter, perfRequirement));
    }

    /**
     * Get and remove test results for particular test method
     * 
     * @param serviceId full test method name including class name
     */
    public TestMethodCompletionResult pollCompletionResult(String serviceId) {
        return completionResults.remove(serviceId);
    }

    public static class TestResult {

        private final int latency;

        private final long startTime;

        public TestResult(int latency, long startTime) {
            this.latency = latency;
            this.startTime = startTime;
        }

        public int getLatency() {
            return latency;
        }

        public long getStartTime() {
            return startTime;
        }
    }

}
