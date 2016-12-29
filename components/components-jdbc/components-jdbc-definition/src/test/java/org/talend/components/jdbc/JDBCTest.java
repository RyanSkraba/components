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
