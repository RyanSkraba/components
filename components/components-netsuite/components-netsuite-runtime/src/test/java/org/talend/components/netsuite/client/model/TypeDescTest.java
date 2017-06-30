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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;

import com.netsuite.webservices.test.lists.accounting.Account;

/**
 *
 */
public class TypeDescTest {

    @Test
    public void testSimpleFieldDesc() {
        SimpleFieldDesc fieldDesc1 = new SimpleFieldDesc("InternalId", String.class, true, true);
        fieldDesc1.setPropertyName("internalId");

        assertEquals("InternalId", fieldDesc1.getName());
        assertEquals("internalId", fieldDesc1.getPropertyName());
        assertEquals(String.class, fieldDesc1.getValueType());
        assertEquals(true, fieldDesc1.isKey());
        assertEquals(true, fieldDesc1.isNullable());

        assertNotNull(fieldDesc1.toString());

        SimpleFieldDesc fieldDesc2 = new SimpleFieldDesc();
        fieldDesc2.setName("Inventory");
        fieldDesc2.setPropertyName("inventory");
        fieldDesc2.setValueType(Boolean.class);
        fieldDesc2.setKey(false);
        fieldDesc2.setNullable(true);

        assertEquals("Inventory", fieldDesc2.getName());
        assertEquals("inventory", fieldDesc2.getPropertyName());
        assertEquals(Boolean.class, fieldDesc2.getValueType());
        assertEquals(false, fieldDesc2.isKey());
        assertEquals(true, fieldDesc2.isNullable());
    }

    @Test
    public void testCustomFieldDesc() {
        CustomFieldDesc fieldDesc1 = new CustomFieldDesc();
        NsRef customizationRef1 = new NsRef(RefType.CUSTOMIZATION_REF);
        customizationRef1.setScriptId("custentity101");
        customizationRef1.setInternalId("10001");
        customizationRef1.setName("Custom Field 1");
        fieldDesc1.setCustomizationRef(customizationRef1);
        fieldDesc1.setName("Custentity101");
        fieldDesc1.setCustomFieldType(CustomFieldRefType.STRING);

        assertEquals("Custentity101", fieldDesc1.getName());
        assertEquals(customizationRef1, fieldDesc1.getCustomizationRef());
        assertEquals(CustomFieldRefType.STRING, fieldDesc1.getCustomFieldType());

        assertNotNull(fieldDesc1.toString());
    }

    @Test
    public void testTypeDesc() {
        SimpleFieldDesc field1 = new SimpleFieldDesc("InternalId", String.class, true, true);
        field1.setPropertyName("internalId");

        SimpleFieldDesc field2 = new SimpleFieldDesc("Inventory", Boolean.class, false, true);
        field2.setPropertyName("inventory");

        CustomFieldDesc field3 = new CustomFieldDesc();
        NsRef customizationRef1 = new NsRef(RefType.CUSTOMIZATION_REF);
        customizationRef1.setScriptId("custentity101");
        customizationRef1.setInternalId("10001");
        customizationRef1.setName("Custom Field 1");
        field3.setCustomizationRef(customizationRef1);
        field3.setName("Custentity101");

        CustomFieldDesc field4 = new CustomFieldDesc();
        NsRef customizationRef2 = new NsRef(RefType.CUSTOMIZATION_REF);
        customizationRef2.setScriptId("custentity102");
        customizationRef2.setInternalId("10002");
        customizationRef2.setName("Custom Field 2");
        field4.setCustomizationRef(customizationRef2);
        field4.setName("Custentity102");

        TypeDesc typeDesc = new TypeDesc("Account", Account.class,
                Arrays.asList(field1, field2, field3, field4));

        assertEquals("Account", typeDesc.getTypeName());
        assertEquals(Account.class, typeDesc.getTypeClass());
        assertEquals(4, typeDesc.getFields().size());
        assertEquals(4, typeDesc.getFieldMap().size());

        List<String> fieldNames = Arrays.asList("InternalId", "Inventory", "Custentity101", "Custentity102");

        for (String fieldName : fieldNames) {
            FieldDesc fieldDesc = typeDesc.getField(fieldName);
            assertNotNull(fieldDesc);
            if (fieldDesc instanceof SimpleFieldDesc) {
                fieldDesc.asSimple();
            } else if (fieldDesc instanceof CustomFieldDesc) {
                fieldDesc.asCustom();
            }
        }

        assertNotNull(typeDesc.toString());
    }
}
