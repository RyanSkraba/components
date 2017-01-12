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
package org.talend.components.jdbc;

import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class JDBCTest extends AbstractComponentTest2 {

    private DefinitionRegistry testComponentRegistry;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        if (testComponentRegistry == null) {
            testComponentRegistry = new DefinitionRegistry();

            testComponentRegistry.registerComponentFamilyDefinition(new JDBCFamilyDefinition());
        }
        return testComponentRegistry;
    }

}
