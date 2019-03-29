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
package org.talend.components.jira;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.ComponentsPaxExamOptions;

/**
 * Integration tests for Jira Input component, which check if nothing was missed
 * during component implementation
 */
@RunWith(DisablablePaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiJiraComponentsTestIT extends JiraComponentsTestBase {

    @Configuration
    public Option[] config() {

        return options(composite(ComponentsPaxExamOptions.getOptions()), //
                linkBundle("commons-logging-commons-logging"), //
                linkBundle("org.talend.components-components-common-bundle"), //
                linkBundle("org.talend.components-components-jira-bundle"));
    }
    // all test cases are to be found in the parent class.
}
