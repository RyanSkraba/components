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

package org.talend.components.netsuite;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.ComponentsPaxExamOptions;

@RunWith(DisablablePaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiNetSuiteComponentTestIT extends NetSuiteComponentTestBase {

    @Configuration
    public Option[] config() {
        return options(composite(ComponentsPaxExamOptions.getOptions()), //
                linkBundle("org.talend.components-components-common-bundle"), //
                linkBundle("org.talend.components-components-netsuite-definition-bundle"));
    }

}
