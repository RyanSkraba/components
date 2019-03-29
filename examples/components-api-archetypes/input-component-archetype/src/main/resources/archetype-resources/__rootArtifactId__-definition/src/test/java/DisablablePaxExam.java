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
package ${package};

import org.apache.commons.lang3.SystemUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.junit.PaxExam;

// not a perfect impl but enough for now if we want to keep that test
public class DisablablePaxExam extends PaxExam {

    private final Class<?> clazz;

    public DisablablePaxExam(final Class<?> klass) throws InitializationError {
        super(klass);
        clazz = klass;
    }

    @Override
    public void run(final RunNotifier notifier) {
        if (!SystemUtils.JAVA_VERSION.startsWith("1.8.")) {
            notifier.fireTestAssumptionFailed(new Failure(
                    Description.createSuiteDescription(clazz),
                    new IllegalStateException("Java " + SystemUtils.JAVA_VERSION + " not yet supported")));
        } else {
            super.run(notifier);
        }
    }
}
