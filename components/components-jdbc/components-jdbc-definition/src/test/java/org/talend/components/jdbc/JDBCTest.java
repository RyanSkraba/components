package org.talend.components.jdbc;

import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest;

public class JDBCTest extends AbstractComponentTest {

    private ComponentServiceImpl componentService;

    /**
     * test the i18n and the basic setting
     */
    @Override
    public ComponentService getComponentService() {
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();

            testComponentRegistry.registerComponentFamilyDefinition(new JDBCFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

}
