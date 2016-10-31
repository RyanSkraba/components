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
package org.talend.components.jira;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.service.spring.SpringTestApp;

/**
 * Integration tests for Jira Input component, which check if nothing was missed
 * during component implementation
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class SpringJiraComponentsTestIT extends JiraComponentsTestBase {
    // all test case are in the parent class.
}
