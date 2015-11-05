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
package org.talend.components.salesforce;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.testcomponent.PropertiesTester;
import org.talend.components.test.SpringApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class SalesforceUITestIT {


    public PropertiesTester propertiesTester;

    @Inject
    public ComponentService componentService;



    public SalesforceUITestIT() {
    }

    @Test public void testBasicUi() {
        propertiesTester = new PropertiesTester();
        propertiesTester.componentService = componentService;
        propertiesTester.processCommands("createProps tSalesforceConnection\nshowProps");

    }

}
