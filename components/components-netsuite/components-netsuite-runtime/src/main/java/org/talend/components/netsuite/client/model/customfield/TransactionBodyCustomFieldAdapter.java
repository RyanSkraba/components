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

import java.util.Map;

import org.talend.components.netsuite.client.model.BasicRecordType;
import org.talend.components.netsuite.client.model.beans.Beans;

import com.google.common.collect.ImmutableMap;

/**
 * Custom field adapter for {@link BasicRecordType#TRANSACTION_BODY_CUSTOM_FIELD} type.
 */
public class TransactionBodyCustomFieldAdapter<T> extends CustomFieldAdapter<T> {

    private static final Map<String, String> recordTypePropertyMap = ImmutableMap.<String, String>builder()
            .put("assemblyBuild", "bodyAssemblyBuild")
            .put("purchaseOrder", "bodyPurchase")
            .put("journalEntry", "bodyJournal")
            .put("expenseReport", "bodyExpenseReport")
            .put("opportunity", "bodyOpportunity")
            .put("itemReceipt", "bodyItemReceipt")
            .put("itemFulfillment", "bodyItemFulfillment")
            .put("inventoryAdjustment", "bodyInventoryAdjustment")
            .put("customerPayment", "bodyCustomerPayment")
            .put("vendorPayment", "bodyVendorPayment")
            .put("vendorBill", "bodyPurchase")
            .build();

    public TransactionBodyCustomFieldAdapter() {
        super(BasicRecordType.TRANSACTION_BODY_CUSTOM_FIELD);
    }

    @Override
    public boolean appliesTo(String recordType, T field) {
        String propertyName = recordTypePropertyMap.get(recordType);
        Boolean applies = propertyName != null ? (Boolean) Beans.getSimpleProperty(field, propertyName) : Boolean.FALSE;
        return applies == null ? false : applies.booleanValue();
    }

    @Override
    public CustomFieldRefType apply(T field) {
        return getFieldType(field);
    }

}
