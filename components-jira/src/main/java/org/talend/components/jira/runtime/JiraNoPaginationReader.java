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
package org.talend.components.jira.runtime;

import java.io.IOException;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;

/**
 * Common {@link JiraReader} for Jira REST API resources, which don't support pagination, e.g. Project resource
 */
public abstract class JiraNoPaginationReader extends JiraReader {

    /**
     * {@inheritDoc}
     */
    public JiraNoPaginationReader(JiraSource source, String hostPort, String resource, String user, String password,
            Map<String, Object> sharedParameters, Schema schema, RuntimeContainer container) {
        super(source, hostPort, resource, user, password, sharedParameters, schema, container);
    }

    /**
     * Does nothing, because of no pagination support. Only 1 request is possible
     * 
     * @throws IOException doens't throw exception
     */
    @Override
    protected void requestMoreRecords() throws IOException {
        // nothing to do
    }
}
