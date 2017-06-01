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
package org.talend.components.api.test.performance;

import org.databene.contiperf.ExecutionLogger;
import org.databene.contiperf.junit.ContiPerfRule;
import org.databene.contiperf.report.CSVSummaryReportModule;
import org.databene.contiperf.report.ReportContext;
import org.databene.contiperf.report.ReportModule;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.talend.components.api.test.performance.report.PerformanceTestsRegistry;
import org.talend.components.api.test.performance.report.PerformanceTestsResultHolder;
import org.talend.components.api.test.performance.report.XmlJUnitReportModule;

/**
 * The purpose of this class is to create adapters for ContiPerf library, so it would be able to work with JUnit 4.12
 * and higher. In JUnit 4.12 "fNext" field name in {@link RunBefores} and {@link RunAfters} was changed to "next". Due
 * to that, ContiPerf is unable to work with JUnit 4.12 or higher, as the Reflection in {@link ContiPerfRule} uses
 * "fNext" field name.
 * 
 * </br>
 * For that purpose, we've created some adapters, which include fields with name "fNext".
 */
public class ContiPerfRuleAdaptor extends ContiPerfRule {

    private static PerformanceTestsRegistry testsRegistry = new PerformanceTestsRegistry();

    private static PerformanceTestsResultHolder testsResultsHolder = new PerformanceTestsResultHolder();

    public ContiPerfRuleAdaptor() {
        super(new CSVSummaryReportModule(), new XmlJUnitReportModule(testsRegistry, testsResultsHolder, false));
    }

    public ContiPerfRuleAdaptor(ExecutionLogger logger) {
        super(logger);
    }

    public ContiPerfRuleAdaptor(ReportModule... modules) {
        super(modules);
    }

    protected ContiPerfRuleAdaptor(ReportContext context) {
        super(context);
    }

    protected ContiPerfRuleAdaptor(ReportContext context, Object suite) {
        super(context, suite);
    }

    /**
     * The purpose of this method is to recreate statement with JUnit 4.12 and higher adapters.
     */
    private Statement recreateStatementWithAdapters(Statement baseStatement, FrameworkMethod method, Object target) {
        testsRegistry.registerClassPerfTestMethods(method.getMethod().getDeclaringClass());
        Statement base = baseStatement;
        RunBefores runBefores = null;
        RunAfters runAfters = null;
        try {
            if (base instanceof RunAfters) {
                runAfters = (RunAfters) base;
                base = ReflectionUtils.getObjectByField(base, base.getClass(), "next");
            }
            if (base instanceof RunBefores) {
                runBefores = (RunBefores) base;
                base = ReflectionUtils.getObjectByField(base, base.getClass(), "next");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            // if runBefores has been removed, reapply it
            if (runBefores != null) {
                runBefores = RunBeforesContiPerfAdapter.create(runBefores, base);
                base = runBefores;
            }
            // if runAfters has been removed, reapply it
            if (runAfters != null) {
                runAfters = RunAftersContiPerfAdapter.create(runAfters, base);
                base = runAfters;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return base;
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return super.apply(recreateStatementWithAdapters(base, method, target), method, target);
    }

}