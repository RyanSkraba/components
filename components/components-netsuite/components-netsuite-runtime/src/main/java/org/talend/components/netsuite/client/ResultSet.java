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

package org.talend.components.netsuite.client;

/**
 * Responsible for sequential retrieving of result data objects.
 */
public abstract class ResultSet<T> {

    /**
     * Advance to next result.
     *
     * @return {@code} true if result is available, {@code false} otherwise
     * @throws NetSuiteException if an error occurs during retrieving of results
     */
    public abstract boolean next() throws NetSuiteException;

    /**
     * Get last read result.
     *
     * @return result object
     * @throws NetSuiteException if an error occurs during retrieving of results
     */
    public abstract T get() throws NetSuiteException;
}
