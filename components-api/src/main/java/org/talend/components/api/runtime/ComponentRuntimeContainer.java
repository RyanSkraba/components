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
package org.talend.components.api.runtime;

import org.talend.components.api.adaptor.ComponentDynamicHolder;

import java.util.Date;
import java.util.Map;

/**
 * The container that's running the component provides this implementation.
 *
 * This handles various functionality in the runtime environment required by components.
 */
public interface ComponentRuntimeContainer {

    // DI global map
    public Map<String, Object> getGlobalMap();

    /**
     * Get name of the component which current object belongs to.
     */
    public String getCurrentComponentName();

    /**
     * Format the specified date according to the specified pattern.
     */
    public String formatDate(Date date, String pattern);

    /**
     * Creates a {@link ComponentDynamicHolder} object.
     */
    public ComponentDynamicHolder createDynamicHolder();
}
