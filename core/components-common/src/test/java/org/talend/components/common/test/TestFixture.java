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

package org.talend.components.common.test;

/**
 * Reusable code which performs setup for a test.
 */
public interface TestFixture {

    /**
     * Perform steps required to prepare testing environment.
     *
     * @throws Exception if an error occurs during setting up
     */
    void setUp() throws Exception;

    /**
     * Perform steps required to return environment to original state after testing.
     *
     * @throws Exception if an error occurs during tearing down
     */
    void tearDown() throws Exception;
}
