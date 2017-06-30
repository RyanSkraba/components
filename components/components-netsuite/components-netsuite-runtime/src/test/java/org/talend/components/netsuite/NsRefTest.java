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

import java.util.UUID;

import org.junit.Test;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TestBasicMetaDataImpl;

import com.netsuite.webservices.test.platform.core.CustomRecordRef;
import com.netsuite.webservices.test.platform.core.CustomizationRef;
import com.netsuite.webservices.test.platform.core.RecordRef;
import com.netsuite.webservices.test.platform.core.types.RecordType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class NsRefTest {

    private BasicMetaData basicMetaData = TestBasicMetaDataImpl.getInstance();

    @Test
    public void testRecordRef() {
        NsRef ref1 = new NsRef(RefType.RECORD_REF);
        ref1.setType(RecordType.ACCOUNT.value());
        ref1.setInternalId("10001");
        ref1.setExternalId(UUID.randomUUID().toString());

        RecordRef recordRef = (RecordRef) ref1.toNativeRef(basicMetaData);
        assertNotNull(recordRef);
        assertEquals(RecordType.ACCOUNT, recordRef.getType());
        assertEquals(ref1.getInternalId(), recordRef.getInternalId());
        assertEquals(ref1.getExternalId(), recordRef.getExternalId());

        NsRef ref2 = NsRef.fromNativeRef(recordRef);
        assertNotNull(ref2);
        assertEquals(RecordType.ACCOUNT.value(), ref2.getType());
        assertEquals(recordRef.getInternalId(), ref2.getInternalId());
        assertEquals(recordRef.getExternalId(), ref2.getExternalId());
    }

    @Test
    public void testCustomRecordRef() {
        NsRef ref = new NsRef(RefType.CUSTOM_RECORD_REF);
        ref.setTypeId("1001");
        ref.setInternalId("10001");
        ref.setExternalId(UUID.randomUUID().toString());
        ref.setScriptId("custrecord25");

        CustomRecordRef customRecordRef = (CustomRecordRef) ref.toNativeRef(basicMetaData);
        assertNotNull(customRecordRef);
        assertNull(ref.getType());
        assertEquals(ref.getTypeId(), customRecordRef.getTypeId());
        assertEquals(ref.getInternalId(), customRecordRef.getInternalId());
        assertEquals(ref.getExternalId(), customRecordRef.getExternalId());
        assertEquals(ref.getScriptId(), customRecordRef.getScriptId());

        NsRef ref2 = NsRef.fromNativeRef(customRecordRef);
        assertNotNull(ref2);
        assertEquals(customRecordRef.getTypeId(), ref2.getTypeId());
        assertEquals(customRecordRef.getInternalId(), ref2.getInternalId());
        assertEquals(customRecordRef.getExternalId(), ref2.getExternalId());
        assertNull(ref2.getScriptId());
    }

    @Test
    public void testCustomizationRef() {
        NsRef ref = new NsRef(RefType.CUSTOMIZATION_REF);
        ref.setType(RecordType.ENTITY_CUSTOM_FIELD.value());
        ref.setInternalId("10001");
        ref.setExternalId(UUID.randomUUID().toString());
        ref.setScriptId("custentity102");
        ref.setName("Custom Entity Field 1");

        CustomizationRef customizationRef = (CustomizationRef) ref.toNativeRef(basicMetaData);
        assertNotNull(customizationRef);
        assertNull(ref.getTypeId());
        assertEquals(RecordType.ENTITY_CUSTOM_FIELD, customizationRef.getType());
        assertEquals(ref.getInternalId(), customizationRef.getInternalId());
        assertEquals(ref.getExternalId(), customizationRef.getExternalId());
        assertEquals(ref.getScriptId(), customizationRef.getScriptId());

        NsRef ref2 = NsRef.fromNativeRef(customizationRef);
        assertNotNull(ref2);
        assertNull(ref2.getType());
        assertEquals(customizationRef.getInternalId(), ref2.getInternalId());
        assertEquals(customizationRef.getExternalId(), ref2.getExternalId());
        assertEquals(customizationRef.getScriptId(), ref2.getScriptId());
    }

    @Test
    public void testEqualsAndHashCode() {
        NsRef ref1 = new NsRef(RefType.RECORD_REF);
        ref1.setType(RecordType.ACCOUNT.value());
        ref1.setInternalId("10001");

        NsRef ref2 = new NsRef(RefType.RECORD_REF);
        ref2.setType(RecordType.ACCOUNT.value());
        ref2.setInternalId("10001");

        assertEquals(ref1, ref2);
        assertEquals(ref1.hashCode(), ref2.hashCode());

        NsRef ref3 = new NsRef(RefType.RECORD_REF);
        ref3.setType(RecordType.ACCOUNT.value());
        ref3.setInternalId("10002");

        assertNotEquals(ref1, ref3);
    }
}
