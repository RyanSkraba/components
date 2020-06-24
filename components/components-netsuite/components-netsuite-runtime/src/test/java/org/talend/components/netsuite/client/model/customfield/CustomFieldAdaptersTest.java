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

package org.talend.components.netsuite.client.model.customfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.components.netsuite.client.model.BasicRecordType;

import com.netsuite.webservices.test.platform.core.RecordRef;
import com.netsuite.webservices.test.platform.core.types.RecordType;
import com.netsuite.webservices.test.setup.customization.CrmCustomField;
import com.netsuite.webservices.test.setup.customization.CustomFieldType;
import com.netsuite.webservices.test.setup.customization.EntityCustomField;
import com.netsuite.webservices.test.setup.customization.ItemCustomField;
import com.netsuite.webservices.test.setup.customization.ItemOptionCustomField;
import com.netsuite.webservices.test.setup.customization.OtherCustomField;
import com.netsuite.webservices.test.setup.customization.TransactionBodyCustomField;
import com.netsuite.webservices.test.setup.customization.TransactionColumnCustomField;
import com.netsuite.webservices.test.setup.customization.types.CustomizationFieldType;

/**
 *
 */
public class CustomFieldAdaptersTest {

    @Test
    public void testAdapterForCrmCustomField() {
        CrmCustomField customField = new CrmCustomField();
        customField.setFieldType(CustomizationFieldType.CHECK_BOX);
        customField.setAppliesToCampaign(true);

        CrmCustomFieldAdapter adapter1 = new CrmCustomFieldAdapter();
        assertTrue(adapter1.appliesTo(RecordType.CAMPAIGN.value(), customField));
        assertEquals(CustomFieldRefType.BOOLEAN, adapter1.apply(customField));

        assertFalse(adapter1.appliesTo(RecordType.OPPORTUNITY.value(), customField));
    }

    @Test
    public void testAdapterForEntityCustomField() {
        EntityCustomField customField = new EntityCustomField();
        customField.setFieldType(CustomizationFieldType.DATETIME);
        customField.setAppliesToPartner(true);
        customField.setAppliesToVendor(true);

        EntityCustomFieldAdapter adapter1 = new EntityCustomFieldAdapter();
        assertTrue(adapter1.appliesTo(RecordType.PARTNER.value(), customField));
        assertTrue(adapter1.appliesTo(RecordType.VENDOR.value(), customField));
        assertEquals(CustomFieldRefType.DATE, adapter1.apply(customField));

        assertFalse(adapter1.appliesTo(RecordType.CONTACT.value(), customField));
    }

    @Test
    public void testAdapterForItemCustomField() {
        ItemCustomField customField = new ItemCustomField();
        customField.setFieldType(CustomizationFieldType.E_MAIL_ADDRESS);
        customField.setAppliesToInventory(true);

        ItemCustomFieldAdapter adapter1 = new ItemCustomFieldAdapter();
        assertTrue(adapter1.appliesTo(RecordType.INVENTORY_ITEM.value(), customField));
        assertEquals(CustomFieldRefType.STRING, adapter1.apply(customField));

        assertFalse(adapter1.appliesTo(RecordType.ITEM_GROUP.value(), customField));
    }

    @Test
    public void testAdapterForItemOptionCustomField() {
        ItemOptionCustomField customField = new ItemOptionCustomField();
        customField.setFieldType(CustomizationFieldType.IMAGE);
        customField.setColPurchase(true);

        ItemOptionCustomFieldAdapter adapter1 = new ItemOptionCustomFieldAdapter();
        assertTrue(adapter1.appliesTo(RecordType.PURCHASE_ORDER.value(), customField));
        assertEquals(CustomFieldRefType.STRING, adapter1.apply(customField));

        assertFalse(adapter1.appliesTo(RecordType.OPPORTUNITY.value(), customField));
    }

    @Test
    public void testAdapterForTransactionBodyCustomField() {
        TransactionBodyCustomField customField = new TransactionBodyCustomField();
        customField.setFieldType(CustomizationFieldType.DECIMAL_NUMBER);
        customField.setBodyOpportunity(true);
        customField.setBodySale(true);

        TransactionBodyCustomFieldAdapter adapter1 = new TransactionBodyCustomFieldAdapter();
        assertTrue(adapter1.appliesTo(RecordType.OPPORTUNITY.value(), customField));
        assertTrue(adapter1.appliesTo(RecordType.SALES_ORDER.value(), customField));
        assertEquals(CustomFieldRefType.DOUBLE, adapter1.apply(customField));

        assertFalse(adapter1.appliesTo(RecordType.CUSTOMER_PAYMENT.value(), customField));
    }

    @Test
    public void testAdapterForTransactionColumnCustomField() {
        TransactionColumnCustomField customField = new TransactionColumnCustomField();
        customField.setFieldType(CustomizationFieldType.CURRENCY);
        customField.setColPurchase(true);

        TransactionColumnCustomFieldAdapter adapter1 = new TransactionColumnCustomFieldAdapter();
        assertTrue(adapter1.appliesTo(RecordType.PURCHASE_ORDER.value(), customField));
        assertEquals(CustomFieldRefType.DOUBLE, adapter1.apply(customField));

        assertFalse(adapter1.appliesTo(RecordType.ITEM_RECEIPT.value(), customField));
    }

    @Test
    public void testOtherCustomFieldAdapter() {
        OtherCustomField customField = new OtherCustomField();
        customField.setFieldType(CustomizationFieldType.TEXT_AREA);
        RecordRef recRef = new RecordRef();
        recRef.setName("account");
        customField.setRecType(recRef);

        OtherCustomFieldAdapter<OtherCustomField> adapter1 = new OtherCustomFieldAdapter<>();
        assertTrue(adapter1.appliesTo("account", customField));
        assertEquals(CustomFieldRefType.STRING, adapter1.apply(customField));
        assertFalse(adapter1.appliesTo("opportunity", customField));
    }
}
