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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.databene.contiperf.PerformanceRequirement;
import org.databene.contiperf.report.AbstractReportModule;
import org.databene.stat.LatencyCounter;
import org.talend.components.api.test.performance.report.PerformanceTestsResultHolder.TestResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Report module for JUnit-like XML Performance tests reports.
 */
public class XmlJUnitReportModule extends AbstractReportModule {

    private static final String TESTCASE = "testcase";

    private static final String FAILURES = "failures";

    private static final String TESTS = "tests";

    private static final String SKIPPED = "skipped";

    private static final String ERRORS = "errors";

    private static final String TIME = "time";

    private static final String NAME = "name";

    private static final String CLASSNAME = "classname";

    private static final String FAILURE = "failure";

    private static final String MESSAGE = "message";

    private static final String TYPE = "type";

    private final PerformanceTestsRegistry testsRegistry;

    private final PerformanceTestsResultHolder resultsHolder;

    private final boolean filePerMethod;

    public XmlJUnitReportModule(PerformanceTestsRegistry testsRegistry, PerformanceTestsResultHolder resultsHolder) {
        this(testsRegistry, resultsHolder, false);
    }

    public XmlJUnitReportModule(PerformanceTestsRegistry testsRegistry, PerformanceTestsResultHolder resultsHolder,
            boolean filePerTestMethod) {
        this.testsRegistry = testsRegistry;
        this.resultsHolder = resultsHolder;
        this.filePerMethod = filePerTestMethod;
    }

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    @Override
    public void invoked(String serviceId, int latency, long startTime) {
        if (testsRegistry.isMethodRegistered(serviceId)) {
            resultsHolder.addResult(serviceId, latency, startTime);
        }
    }

    public void completed(String serviceId, LatencyCounter[] counters, PerformanceRequirement requirement) {
        if (!testsRegistry.isMethodRegistered(serviceId)) {
            return;
        }
        resultsHolder.addCompletionResult(testsRegistry.getMethodDefinitionById(serviceId), counters[0], requirement);
        testsRegistry.methodInvokationCompleted(serviceId);
        if (!testsRegistry.isTestClassFinished(serviceId)) {
            return;
        }
        TestMethodReportDefinition latestDefinition = testsRegistry.getMethodDefinitionById(serviceId);
        String className = latestDefinition.getClassName();
        printResults(className);
    }

    private void printResults(String className) {
        Set<TestMethodReportDefinition> methodDefinitions = testsRegistry.removeClassTestMethods(className);
        Document doc = getDocumentBuilder().newDocument();
        Element rootElement = null;
        long totalTime = 0;
        int testsTotal = 0;
        int testsFailed = 0;
        for (TestMethodReportDefinition def : methodDefinitions) {
            testsRegistry.removeMethodDefinitionById(def.getFullMethodName());
            if (filePerMethod || rootElement == null) {
                rootElement = createTestSuiteElement(className, doc);
                totalTime = 0;
                testsTotal = 0;
                testsFailed = 0;
            }
            TestMethodCompletionResult completionResult = resultsHolder.pollCompletionResult(def.getFullMethodName());
            LatencyCounter mainCounter = completionResult.getMainCounter();
            PerformanceRequirement requirement = completionResult.getPerfRequirement();
            List<TestResult> testResults = null;
            testResults = resultsHolder.pollTestResults(def.getFullMethodName());
            if (testResults != null) {
                testsTotal += testResults.size();
                for (TestResult result : testResults) {
                    Element currentTest = doc.createElement(TESTCASE);
                    rootElement.appendChild(currentTest);
                    boolean isFailed = formatTestCase(def, currentTest, result, requirement, doc);
                    if (isFailed) {
                        testsFailed++;
                    }
                }
            }
            totalTime += mainCounter.totalLatency();
            if (filePerMethod) {
                rootElement.setAttribute(TIME, String.valueOf(totalTime / 1000.0));
                rootElement.setAttribute(TESTS, "" + testsTotal);
                rootElement.setAttribute(FAILURES, "" + testsFailed);
                printElement(rootElement, def.getFullMethodName());
            }
        }
        if (!filePerMethod) {
            rootElement.setAttribute(TIME, String.valueOf(totalTime / 1000.0));
            rootElement.setAttribute(TESTS, "" + testsTotal);
            rootElement.setAttribute(FAILURES, "" + testsFailed);
            printElement(rootElement, className);
        }
    }

    private void printElement(Element rootElement, String fileSuffix) {
        File f = new File(filename(fileSuffix));
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fos != null) {
            Writer wri = null;
            try {
                wri = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
                TransformerFactory transFactory = TransformerFactory.newInstance();
                Transformer transformer = transFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                StringWriter buffer = new StringWriter();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.transform(new DOMSource(rootElement), new StreamResult(buffer));
                String str = buffer.toString();
                wri.write(str);
                wri.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } finally {
                if (wri != null) {
                    try {
                        wri.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean formatTestCase(TestMethodReportDefinition def, Element currentTest, TestResult result,
            PerformanceRequirement requirement, Document doc) {
        boolean isFailed = false;
        currentTest.setAttribute(NAME, def.getMethodName());
        currentTest.setAttribute(CLASSNAME, def.getClassName());
        currentTest.setAttribute(TIME, "" + (result.getLatency() / 1000.0));
        if (requirement != null) {
            try {
                checkTestResult(result, requirement);
            } catch (Throwable e) {
                isFailed = true;
                appendFailure(def, currentTest, e, doc);
            }
        }
        return isFailed;
    }

    private void appendFailure(TestMethodReportDefinition def, Element currentTest, Throwable e, Document doc) {
        Element failure = doc.createElement(FAILURE);
        currentTest.appendChild(failure);
        failure.setAttribute(MESSAGE, e.getMessage());
        failure.setAttribute(TYPE, e.getClass().getName());
        String strace = getStackTraceAsString(e);
        Text trace = doc.createTextNode(strace);
        failure.appendChild(trace);
    }

    private String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

    private void checkTestResult(TestResult result, PerformanceRequirement requirement) {
        long requiredMax = requirement.getMax();
        if (requiredMax >= 0) {
            if (result.getLatency() > requiredMax) {
                context.fail(
                        "The maximum latency of " + requiredMax + " ms was exceeded, Measured: " + result.getLatency() + " ms");
            }
        }
    }

    protected Element createTestSuiteElement(String className, Document doc) {
        Element rootElement = doc.createElement("testsuite");
        String n = className;
        rootElement.setAttribute(NAME, n == null ? "unknown" : n);
        rootElement.setAttribute(ERRORS, "0");
        rootElement.setAttribute(SKIPPED, "0");
        return rootElement;
    }

    private String filename(String serviceId) {
        return context.getReportFolder() + File.separator + "PERF-TEST-" + serviceId + ".xml";
    }

}
