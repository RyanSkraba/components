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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.datum.Entity;


/**
 * {@link JiraReader} for /rest/api/2/project/{projectIdOrKey} Jira REST API resource
 */
public class JiraProjectIdReader extends JiraNoPaginationReader {

    /**
     * {@inheritDoc}
     */
    public JiraProjectIdReader(JiraSource source, String hostPort, String resource, String user, String password,
            Map<String, Object> sharedParameters, Schema schema, RuntimeContainer container) {
        super(source, hostPort, resource, user, password, sharedParameters, schema, container);
    }

    /**
     * Wraps response into entity and returns {@link List} with only 1 entity
     * It doesn't need to make additional action, because Jira server already
     * returns single project json
     * 
     * @param response http response
     * @return {@link List} of entities retrieved from response
     */
    @Override
    protected List<Entity> processResponse(String response) {
        Entity entity = new Entity(response);
        return Collections.singletonList(entity);
    }

}
