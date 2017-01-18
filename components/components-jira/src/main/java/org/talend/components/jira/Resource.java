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
package org.talend.components.jira;

/**
 * Jira REST resources
 */
public enum Resource {
    ISSUE {

        @Override
        public String getUrl() {
            return API_VERSION + "issue";
        }

    },
    PROJECT {

        @Override
        public String getUrl() {
            return API_VERSION + "project";
        }

    };

    private static final String API_VERSION = "rest/api/2/";

    public abstract String getUrl();
}
