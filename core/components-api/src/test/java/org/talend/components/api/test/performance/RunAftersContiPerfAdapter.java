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

import java.lang.reflect.Field;
import java.util.List;

import org.junit.internal.runners.statements.RunAfters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Adapter for RunAfters class to be used with ContiPerf and JUnit 4.12 and higher.
 */
public class RunAftersContiPerfAdapter extends RunAfters {

    private final Statement fNext;

    public RunAftersContiPerfAdapter(Statement next, List<FrameworkMethod> afters, Object target) {
        super(next, afters, target);
        this.fNext = next;
    }

    @Override
    public void evaluate() throws Throwable {
        // As the fNext field is set from ContiPerfRule using reflection, we need to set the underlying next field in
        // order the Statement to work
        Field next = this.getClass().getSuperclass().getDeclaredField("next");
        next.setAccessible(true);
        next.set(this, fNext);
        super.evaluate();
    }

    public static RunAftersContiPerfAdapter create(RunAfters runBefores, Statement next)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        List<FrameworkMethod> befores = ReflectionUtils.getObjectByField(runBefores, runBefores.getClass(), "afters");
        Object target = ReflectionUtils.getObjectByField(runBefores, runBefores.getClass(), "target");
        return new RunAftersContiPerfAdapter(next, befores, target);
    }

}
