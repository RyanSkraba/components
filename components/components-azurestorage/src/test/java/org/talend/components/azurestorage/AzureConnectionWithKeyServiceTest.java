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
package org.talend.components.azurestorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AzureConnectionWithKeyServiceTest {

    @Test
    public void testBuildInsatnce() {
        AzureConnectionWithKeyService instance = AzureConnectionWithKeyService.builder()//
                .protocol("http")//
                .accountName("talendAccount")//
                .accountKey("aValide64baseKey")//
                .build();

        assertNotNull(instance);
        assertEquals("http", instance.getProtocol());
        assertEquals("talendAccount", instance.getAccountName());
        assertEquals("aValide64baseKey", instance.getAccountKey());
    }
}
