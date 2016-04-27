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
package org.talend.components.splunk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.service.AbstractComponentTest;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.SimpleComponentRegistry;

/**
 * created by dmytro.chmyga on Apr 27, 2016
 */
public class TSplunkEventCollectorTestBase extends AbstractComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
    
    private ComponentService componentService;
    
    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }
    
    @Override
    public ComponentService getComponentService() {
        if (componentService == null) {
            SimpleComponentRegistry testComponentRegistry = new SimpleComponentRegistry();
            testComponentRegistry.addComponent(TSplunkEventCollectorDefinition.COMPONENT_NAME, new TSplunkEventCollectorDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }
    
}
