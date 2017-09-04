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
package org.talend.components.common.oauth.properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.test.PropertiesTestUtils;

public class Oauth2ImplicitFlowPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void i18nTest() {

        Class propertiesClass = Oauth2ImplicitFlowProperties.class;
        Properties props = PropertiesImpl.createNewInstance(propertiesClass, "root").init();
        // check all properties
        PropertiesTestUtils.checkAllI18N(props, errorCollector);
    }

}
