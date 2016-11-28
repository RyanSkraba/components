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
package org.talend.components.service.rest;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.daikon.definition.service.DefinitionRegistryService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SpringComponentTestIT extends AbstractComponentTest2 {

    @Inject
    private DefinitionRegistryService definitionRegistry;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        return definitionRegistry;
    }

}
