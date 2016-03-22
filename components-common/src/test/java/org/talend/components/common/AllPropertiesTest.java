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
package org.talend.components.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;

public class AllPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testAlli18n() {
        ComponentTestUtils.checkAllI18N(new ProxyProperties(null).init(), errorCollector);
        ComponentTestUtils.checkAllI18N(new SchemaProperties(null).init(), errorCollector);
        ComponentTestUtils.checkAllI18N(new UserPasswordProperties(null).init(), errorCollector);
    }

}
