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

import org.talend.components.netsuite.NetSuiteVersion;

/**
 * Responsible for creation of NetSuite client.
 *
 * @param <T> type of NetSuite web service port
 */
public interface NetSuiteClientFactory<T> {

    /**
     * Create NetSuite client.
     *
     * @return NetSuite client
     * @throws NetSuiteException if error occurs during creation
     */
    NetSuiteClientService<T> createClient() throws NetSuiteException;

    /**
     * Get version of NetSuite runtime.
     *
     * @return version of NetSuite
     */
    NetSuiteVersion getApiVersion();

}
