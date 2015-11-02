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

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.service.ComponentService;
import org.talend.components.common.ProxyPropertiesTest;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestComponentCommonIT extends ProxyPropertiesTest {

    @Inject
    ComponentService osgiCompService;

    @Configuration
    public Option[] config() {

        return PaxExamOptions.getOptions();
        // these debug option do not work, I still don't know how to debug this :, cleanCaches(),
        // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005")
        // , systemTimeout(0)
    }

    @Before
    public void setypComponentService() {
        componentService = osgiCompService;
    }

}
