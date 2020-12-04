// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
    private static final String MAPPING_SALE = "bodySale";

    private static final String MAPPING_PURCHASE = "bodyPurchase";

    private static final String MAPPING_ASSEMBLYBUILD = "bodyAssemblyBuild";

    private static final String MAPPING_CUSTOMERPAYMENT = "bodyCustomerPayment";

    private static final String MAPPING_EXPENSEREPORT = "bodyExpenseReport";

    private static final String MAPPING_INVENTORYADJUSTMENT = "bodyInventoryAdjustment";

    private static final String MAPPING_ITEMFULFILLMENT = "bodyItemFulfillment";

    private static final String MAPPING_ITEMRECEIPT = "bodyItemReceipt";

    private static final String MAPPING_JOURNAL = "bodyJournal";

    private static final String MAPPING_OPPORTUNITY = "bodyOpportunity";

    private static final String MAPPING_TRANSFERORDER = "bodyTransferOrder";

    private static final String MAPPING_VENDORPAYMENT = "bodyVendorPayment";

    private static final Map<String, String> recordTypePropertyMap = ImmutableMap.<String, String>builder()
            .put("assemblyBuild", MAPPING_ASSEMBLYBUILD)
            .put("cashRefund", MAPPING_SALE)
            .put("cashSale", MAPPING_SALE)
            .put("check", MAPPING_PURCHASE)
            .put("creditMemo", MAPPING_SALE)
            .put("customerDeposit", MAPPING_SALE)
            .put("customerPayment", MAPPING_CUSTOMERPAYMENT)
            .put("customerRefund", MAPPING_SALE)
            .put("deposit", MAPPING_SALE)
            .put("depositApplication", MAPPING_SALE)
            .put("estimate", MAPPING_SALE)
            .put("expenseReport", MAPPING_EXPENSEREPORT)
            .put("inventoryAdjustment", MAPPING_INVENTORYADJUSTMENT)
            .put("inventoryCostRevaluation", MAPPING_INVENTORYADJUSTMENT)
            .put("inventoryTransfer", MAPPING_INVENTORYADJUSTMENT)
            .put("invoice", MAPPING_SALE)
            .put("itemFulfillment", MAPPING_ITEMFULFILLMENT)
            .put("itemReceipt", MAPPING_ITEMRECEIPT)
            .put("journal", MAPPING_JOURNAL)
            .put("opportunity", MAPPING_OPPORTUNITY)
            .put("purchaseOrder", MAPPING_PURCHASE)
            .put("returnAuthorization", MAPPING_SALE)
            .put("salesOrder", MAPPING_SALE)
            .put("transferOrder", MAPPING_TRANSFERORDER)
            .put("vendorBill", MAPPING_PURCHASE)
            .put("vendorCredit", MAPPING_PURCHASE)
            .put("vendorPayment", MAPPING_VENDORPAYMENT)
            .put("vendorReturnAuthorization", MAPPING_PURCHASE)
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
