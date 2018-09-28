// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common.tableaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class DefaultSQLTruncateTableAction extends TableAction {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultSQLTruncateTableAction.class);

    private String[] fullTableName;

    public DefaultSQLTruncateTableAction(final String[] fullTableName) {
        if (fullTableName == null || fullTableName.length < 1) {
            throw new InvalidParameterException("Table name can't null or empty");
        }

        this.fullTableName = fullTableName;

    }

    @Override
    public List<String> getQueries() throws Exception {
        List<String> queries = new ArrayList<>();

        queries.add(getTruncateTableQuery());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated SQL queries to truncate table:");
            for (String q : queries) {
                LOG.debug(q);
            }
        }

        return queries;
    }

    private String getTruncateTableQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getConfig().SQL_TRUNCATE_PREFIX);
        sb.append(this.getConfig().SQL_TRUNCATE);
        sb.append(" ");
        sb.append(buildFullTableName(fullTableName, this.getConfig().SQL_FULL_NAME_SEGMENT_SEP, true));
        sb.append(this.getConfig().SQL_TRUNCATE_SUFFIX);
        return sb.toString();
    }

}
