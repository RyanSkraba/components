// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api;

import static org.ops4j.pax.exam.CoreOptions.*;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.service.ComponentService;
import org.talend.components.salesforce.SalesforceComponentTestIT;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SalesfoceComponentsTestIT extends SalesforceComponentTestIT {

    @Inject
    ComponentService osgiCompService;

    @Configuration
    public Option[] config() {
        // Change "-DX" to "-X" below to use the debugger on the test proces
        return options(vmOptions("-Xmx1000m", "-DXrunjdwp:transport=dt_socket,server=y,suspend=y,address=5010"),
                systemTimeout(1000000), composite(PaxExamOptions.getOptions()),
                propagateSystemProperties("salesforce.user", "salesforce.password", "salesforce.key"));
    }

    @Override
    public ComponentService getComponentService() {
        return osgiCompService;
    }

}
