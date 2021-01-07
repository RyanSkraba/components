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
package org.talend.components.api.service.common.testcomponent.inject;

import org.talend.components.api.properties.ComponentPropertiesImpl;

public class TestNestedInjectComponentProperties extends ComponentPropertiesImpl {

    private TestInjectComponentProperties nestedProps = new TestInjectComponentProperties("nestedProps");

    public TestNestedInjectComponentProperties(String name) {
        super(name);
    }

    public TestInjectComponentProperties getNestedProperties() {
        return nestedProps;
    }

}
