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
package org.talend.components.snowflake.test;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.service.spring.SpringTestApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class SpringSnowflakeTestIT extends SnowflakeTestIT {

    @Inject
    ComponentService componentService;

    @Override
    public ComponentService getComponentService() {
        return componentService;
    }

}
