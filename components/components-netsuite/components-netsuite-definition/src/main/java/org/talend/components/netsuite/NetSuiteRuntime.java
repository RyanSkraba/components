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

package org.talend.components.netsuite;

import org.talend.daikon.properties.ValidationResult;

/**
 * Provides functionality required for components in design time.
 */
public interface NetSuiteRuntime {

    /**
     * Set context for runtime object.
     *
     * @param context context to be used
     */
    void setContext(Context context);

    /**
     * Get context used by this runtime object.
     *
     * @return context
     */
    Context getContext();

    /**
     * Get {@link NetSuiteDatasetRuntime} for given connection properties.
     *
     * @param properties connection properties
     * @return {@code NetSuiteDatasetRuntime} object
     */
    NetSuiteDatasetRuntime getDatasetRuntime(NetSuiteProvideConnectionProperties properties);

    /**
     * Validate connection for given connection properties.
     *
     * @param properties connection properties
     * @return result of validation
     */
    ValidationResult validateConnection(NetSuiteProvideConnectionProperties properties);

    /**
     * Context of runtime object.
     */
    interface Context {

        /**
         * Specifies whether runtime should cache connection and related data
         * and reuse it.
         *
         * @return
         */
        boolean isCachingEnabled();

        /**
         * Get value of an attribute stored in the context.
         *
         * @param key key of an attribute
         * @return value or {@code null}
         */
        Object getAttribute(String key);

        /**
         * Store value for a given attribute.
         *
         * @param key key of attribute
         * @param value value to be stored
         */
        void setAttribute(String key, Object value);
    }
}
