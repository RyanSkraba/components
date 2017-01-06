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
package org.talend.components.jira.testutils;

import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.runtime.JiraSink;
import org.talend.components.jira.runtime.JiraWriteOperation;
import org.talend.components.jira.runtime.writer.JiraWriter;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;

/**
 * Provides helper methods for Jira components tests
 */
public final class JiraTestsHelper {
    
    /**
     * Creates {@link JiraWriter}, which will be tested
     * 
     * @param hostPort Jira server url. Example: "http://192.168.99.100:8080/"
     * @param user Jira user
     * @param pass Jira user password
     * @param resource Project or Issue
     * @param action Insert/Update/Delete action
     */
    public static JiraWriter createWriter(String hostPort, String user, String pass, Resource resource, Action action) {
        
        TJiraOutputProperties properties = new TJiraOutputProperties("root");
        properties.init();
        properties.connection.hostUrl.setValue(hostPort);
        properties.connection.basicAuthentication.userId.setValue(user);
        properties.connection.basicAuthentication.password.setValue(pass);
        properties.resource.setValue(resource);
        properties.action.setValue(action);
           
        JiraSink sink = new JiraSink();
        sink.initialize(null, properties);

        JiraWriteOperation writeOperation = (JiraWriteOperation) sink.createWriteOperation();
        writeOperation.initialize(null);
        
        JiraWriter jiraWriter = writeOperation.createWriter(null);
        return jiraWriter;
    }
}
