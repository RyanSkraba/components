// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.runtimeservice.RuntimeUtil;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ComponentsPaxExamOptionsTest {

    @Configuration
    public Option[] config() {
        return ComponentsPaxExamOptions.getOptions();
    }

    @Test
    public void test() {
        // just check one class of each bundle added in the pax exam option can be instantiated.
        // daikon
        new Properties();
        // components-api
        new ComponentPropertiesImpl("foo");
        // api-service
        new ComponentTestUtils();
        // runtime service
        new RuntimeUtil();
    }

}
