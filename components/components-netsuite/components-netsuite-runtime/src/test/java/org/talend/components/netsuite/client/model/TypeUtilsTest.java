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

package org.talend.components.netsuite.client.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.talend.components.netsuite.client.NetSuiteException;

import com.netsuite.webservices.test.lists.accounting.Account;
import com.netsuite.webservices.test.platform.core.BaseRef;
import com.netsuite.webservices.test.platform.core.CustomRecordRef;
import com.netsuite.webservices.test.platform.core.CustomTransactionRef;
import com.netsuite.webservices.test.platform.core.CustomizationRef;
import com.netsuite.webservices.test.platform.core.InitializeAuxRef;
import com.netsuite.webservices.test.platform.core.InitializeRef;
import com.netsuite.webservices.test.platform.core.RecordRef;

/**
 *
 */
public class TypeUtilsTest {

    @Test
    public void testCollectXmlTypes() {
        Set<Class<?>> classes = new HashSet<>();

        TypeUtils.collectXmlTypes(BaseRef.class, BaseRef.class, classes);
        TypeUtils.collectXmlTypes(RecordRef.class, RecordRef.class, classes);

        assertThat(classes, containsInAnyOrder(
                (Class<?>) CustomRecordRef.class,
                CustomTransactionRef.class,
                RecordRef.class,
                InitializeRef.class,
                InitializeAuxRef.class,
                CustomizationRef.class));
    }

    @Test
    public void testCreateInstance() {
        Account instance = TypeUtils.createInstance(Account.class);

        assertNotNull(instance);
    }

    @Test(expected = NetSuiteException.class)
    public void testCreateInstanceError() {
        TypeUtils.createInstance(BaseRef.class);
    }
}
