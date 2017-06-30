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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.test.client.model.TestRecordTypeEnum;

/**
 *
 */
public class RecordTypeInfoTest {

    @Test
    public void testRecordTypeInfo() {
        RecordTypeInfo info = new RecordTypeInfo(TestRecordTypeEnum.ACCOUNT);

        assertEquals(TestRecordTypeEnum.ACCOUNT, info.getRecordType());
        assertEquals(TestRecordTypeEnum.ACCOUNT.getTypeName(), info.getName());
        assertEquals(TestRecordTypeEnum.ACCOUNT.getTypeName(), info.getDisplayName());
        assertEquals(RefType.RECORD_REF, info.getRefType());
        assertNotNull(info.toString());
    }

    @Test
    public void testCustomRecordTypeInfo() {
        NsRef customizationRef = new NsRef(RefType.CUSTOMIZATION_REF);
        customizationRef.setName("Custom Record Type 1");
        customizationRef.setScriptId("custom_record_type_1");
        customizationRef.setInternalId("12345");

        CustomRecordTypeInfo info = new CustomRecordTypeInfo("custom_record_type_1",
                TestRecordTypeEnum.CUSTOM_RECORD, customizationRef);

        assertEquals(TestRecordTypeEnum.CUSTOM_RECORD, info.getRecordType());
        assertEquals("custom_record_type_1", info.getName());
        assertEquals("Custom Record Type 1", info.getDisplayName());
        assertEquals(RefType.CUSTOM_RECORD_REF, info.getRefType());
        assertEquals(customizationRef, info.getCustomizationRef());
        assertNotNull(info.toString());
    }
}
