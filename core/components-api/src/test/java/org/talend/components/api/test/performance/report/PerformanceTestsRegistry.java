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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.test.performance.ReflectionUtils;

/**
 * This is the registry for Performance tests. It can be used on Per test/test class/test suite basis.
 */
public class PerformanceTestsRegistry {

    private final Map<String, Set<String>> classPerfTests = new HashMap<>();

    private final Map<String, Set<TestMethodReportDefinition>> finishedClassPerfTests = new HashMap<>();

    private final Map<String, TestMethodReportDefinition> reportDefinitionsById = new HashMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * This method registers all the test methods, which are not ignored, from the test class in this registry.
     * 
     * @param testClass test class containing performance tests.
     */
    public void registerClassPerfTestMethods(Class<?> testClass) {
        try {
            lock.readLock().lock();
            if (classPerfTests.containsKey(testClass.getName())) {
                return;
            }
        } finally {
            lock.readLock().unlock();
        }
        for (Method m : testClass.getDeclaredMethods()) {
            if (ReflectionUtils.isMethodAnnotated(m, Test.class) && !ReflectionUtils.isMethodAnnotated(m, Ignore.class)) {
                registerMethod(testClass.getName(), m.getName());
            }
        }
    }

    private void registerMethod(String className, String methodName) {
        try {
            lock.writeLock().lock();
            Set<String> classMethods = classPerfTests.get(className);
            if (classMethods == null) {
                classMethods = new HashSet<>();
                classPerfTests.put(className, classMethods);
            }
            classMethods.add(methodName);
            addTestMethodReportDefinition(methodName, className);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Check whether the method is registered in this tests registry.
     * 
     * @param serviceId full method name, containing fully qualified class name
     */
    public boolean isMethodRegistered(String serviceId) {
        try {
            lock.readLock().lock();
            return reportDefinitionsById.containsKey(serviceId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Notify registry that all iterations for the method were finished.
     * 
     * @param serviceId full method name, containing fully qualified class name
     */
    public void methodInvokationCompleted(String serviceId) {
        if (!isMethodRegistered(serviceId)) {
            return;
        }
        TestMethodReportDefinition definition = null;
        try {
            lock.readLock().lock();
            definition = reportDefinitionsById.get(serviceId);
        } finally {
            lock.readLock().unlock();
        }
        if (definition == null) {
            return;
        }
        moveMethodToFinished(definition);
    }

    /**
     * Move method from registered methods to finished methods registry.
     * 
     * @param definition definition of the method
     */
    protected void moveMethodToFinished(TestMethodReportDefinition definition) {
        Set<String> methodsForClass = null;
        try {
            lock.writeLock().lock();
            methodsForClass = classPerfTests.get(definition.getClassName());
            if (methodsForClass == null) {
                return;
            }
            methodsForClass.remove(definition.getMethodName());
            if (methodsForClass.isEmpty()) {
                classPerfTests.remove(definition.getClassName());
            }
            Set<TestMethodReportDefinition> finishedMethodsDefs = finishedClassPerfTests.get(definition.getClassName());
            if (finishedMethodsDefs == null) {
                finishedMethodsDefs = new HashSet<>();
                finishedClassPerfTests.put(definition.getClassName(), finishedMethodsDefs);
            }
            finishedMethodsDefs.add(definition);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns method definition by method full name.
     * 
     * @param serviceId method full name
     * @return TestMethodReportDefition object
     */
    public TestMethodReportDefinition getMethodDefinitionById(String serviceId) {
        try {
            lock.writeLock().lock();
            return reportDefinitionsById.get(serviceId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove method definition from registry by its full name
     * 
     * @param serviceId method full name
     * @return TestMethodReportDefition object
     */
    public TestMethodReportDefinition removeMethodDefinitionById(String serviceId) {
        try {
            lock.writeLock().lock();
            return reportDefinitionsById.remove(serviceId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if all the performance test methods of the class containing this particular method were finished.
     * 
     * @param serviceId method full name
     * @return true if all performance tests were finished
     */
    public boolean isTestClassFinished(String serviceId) {
        try {
            lock.readLock().lock();
            TestMethodReportDefinition def = reportDefinitionsById.get(serviceId);
            return !classPerfTests.containsKey(def.getClassName());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds report definition to registry
     * 
     * @param methodName method short name className name of the class containing
     */
    public void addTestMethodReportDefinition(String methodName, String className) {
        TestMethodReportDefinition def = new TestMethodReportDefinition(className, methodName);
        reportDefinitionsById.put(def.getFullMethodName(), def);
    }

    /**
     * Remove methods for the class from finished methods registry by class name
     * 
     * @param className name of the test class
     * @return Set of the test method definitions
     */
    public Set<TestMethodReportDefinition> removeClassTestMethods(String className) {
        try {
            lock.writeLock().lock();
            return finishedClassPerfTests.remove(className);
        } finally {
            lock.writeLock().unlock();
        }
    }

}
