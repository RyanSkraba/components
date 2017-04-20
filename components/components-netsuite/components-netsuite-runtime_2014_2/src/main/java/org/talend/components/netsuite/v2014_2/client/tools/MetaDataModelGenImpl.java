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

package org.talend.components.netsuite.v2014_2.client.tools;

import java.io.File;
import java.util.Arrays;

import org.talend.components.netsuite.client.tools.MetaDataModelGen;

import com.google.common.collect.ImmutableMap;
import com.netsuite.webservices.v2014_2.platform.core.Record;
import com.netsuite.webservices.v2014_2.platform.core.RecordRef;
import com.netsuite.webservices.v2014_2.platform.core.SearchRecord;
import com.netsuite.webservices.v2014_2.platform.core.SearchRecordBasic;
import com.netsuite.webservices.v2014_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2014_2.platform.core.types.SearchRecordType;
import com.squareup.javapoet.ClassName;

/**
 *
 */
public class MetaDataModelGenImpl extends MetaDataModelGen {

    public MetaDataModelGenImpl() {
        recordTypeEnumClassName = ClassName.get(
                "org.talend.components.netsuite.v2014_2.client.model", "RecordTypeEnum");
        searchRecordTypeEnumClassName = ClassName.get(
                "org.talend.components.netsuite.v2014_2.client.model", "SearchRecordTypeEnum");

        outputFolder = new File("./components/components-netsuite/components-netsuite-runtime_2014_2/src/main/java");

        standardEntityTypes.addAll(Arrays.asList(
                "Account",
                "AccountingPeriod",
                "AppDefinition",
                "AppPackage",
                "BillingAccount",
                "BillingSchedule",
                "Bin",
                "Budget",
                "CalendarEvent",
                "Campaign",
                "Charge",
                "Classification",
                "Contact",
                "ContactCategory",
                "ContactRole",
                "CouponCode",
                "CurrencyRate",
                "Customer",
                "CustomerCategory",
                "CustomerMessage",
                "CustomerStatus",
                "CustomList",
                "CustomRecord",
                "CustomTransaction",
                "Department",
                "Employee",
                "EntityGroup",
                "ExpenseCategory",
                "FairValuePrice",
                "File",
                "Folder",
                "GiftCertificate",
                "GlobalAccountMapping",
                "InventoryNumber",
                "Issue",
                "ItemAccountMapping",
                "ItemDemandPlan",
                "ItemRevision",
                "ItemSupplyPlan",
                "Job",
                "JobStatus",
                "JobType",
                "Location",
                "ManufacturingCostTemplate",
                "ManufacturingOperationTask",
                "ManufacturingRouting",
                "Message",
                "Nexus",
                "Note",
                "NoteType",
                "OtherNameCategory",
                "Partner",
                "PartnerCategory",
                "PaymentMethod",
                "PayrollItem",
                "PhoneCall",
                "PriceLevel",
                "PricingGroup",
                "ProjectTask",
                "PromotionCode",
                "ResourceAllocation",
                "RevRecSchedule",
                "RevRecTemplate",
                "SalesRole",
                "SiteCategory",
                "Solution",
                "Subsidiary",
                "SupportCase",
                "Task",
                "Term",
                "TimeBill",
                "TimeEntry",
                "TimeSheet",
                "Topic",
                "UnitsType",
                "Usage",
                "Vendor",
                "VendorCategory",
                "WinLossReason"
        ));
        standardTransactionTypes.addAll(Arrays.asList(
                "AccountingTransaction",
                "AssemblyBuild",
                "AssemblyUnbuild",
                "BinTransfer",
                "BinWorksheet",
                "CashRefund",
                "CashSale",
                "Check",
                "CreditMemo",
                "CustomerDeposit",
                "CustomerPayment",
                "CustomerRefund",
                "Deposit",
                "DepositApplication",
                "Estimate",
                "ExpenseReport",
                "InterCompanyJournalEntry",
                "InventoryAdjustment",
                "InventoryCostRevaluation",
                "InventoryTransfer",
                "Invoice",
                "ItemFulfillment",
                "ItemReceipt",
                "JournalEntry",
                "Opportunity",
                "PaycheckJournal",
                "PurchaseOrder",
                "ReturnAuthorization",
                "SalesOrder",
                "State",
                "TransferOrder",
                "VendorBill",
                "VendorCredit",
                "VendorPayment",
                "VendorReturnAuthorization",
                "WorkOrder",
                "WorkOrderClose",
                "WorkOrderCompletion",
                "WorkOrderIssue"
        ));
        standardItemTypes.addAll(Arrays.asList(
                "AssemblyItem",
                "BudgetCategory",
                "CampaignAudience",
                "CampaignCategory",
                "CampaignChannel",
                "CampaignFamily",
                "CampaignOffer",
                "CampaignResponse",
                "CampaignSearchEngine",
                "CampaignSubscription",
                "CampaignVertical",
                "CostCategory",
                "Currency",
                "DescriptionItem",
                "DiscountItem",
                "DownloadItem",
                "GiftCertificateItem",
                "InterCompanyTransferOrder",
                "InventoryItem",
                "ItemGroup",
                "KitItem",
                "LandedCost",
                "LeadSource",
                "LotNumberedAssemblyItem",
                "LotNumberedInventoryItem",
                "MarkupItem",
                "NonInventoryPurchaseItem",
                "NonInventoryResaleItem",
                "NonInventorySaleItem",
                "OtherChargePurchaseItem",
                "OtherChargeResaleItem",
                "OtherChargeSaleItem",
                "PaymentItem",
                "SalesTaxItem",
                "SerializedAssemblyItem",
                "SerializedInventoryItem",
                "ServicePurchaseItem",
                "ServiceResaleItem",
                "ServiceSaleItem",
                "StatisticalJournalEntry",
                "SubtotalItem",
                "SupportCaseIssue",
                "SupportCaseOrigin",
                "SupportCasePriority",
                "SupportCaseStatus",
                "SupportCaseType",
                "TaxAcct",
                "TaxGroup",
                "TaxType"
        ));

        additionalRecordTypes.putAll(ImmutableMap.<String, String>builder()
                .put("InventoryDetail", "INVENTORY_DETAIL")
                .put("TimeEntry", "TIME_ENTRY")
                .build());

        additionalSearchRecordTypes.putAll(ImmutableMap.<String, String>builder()
                .put("inventoryDetail", "INVENTORY_DETAIL")
                .put("timeEntry", "TIME_ENTRY")
                .build());

        additionalRecordTypeSearchMappings.putAll(ImmutableMap.<String, String>builder()
                .put("CustomTransaction", "transaction")
                .build());

        setRecordBaseClass(Record.class);
        setRecordTypeEnumClass(RecordType.class);

        setSearchRecordBaseClasses(Arrays.<Class<?>>asList(SearchRecord.class, SearchRecordBasic.class));
        setSearchRecordTypeEnumClass(SearchRecordType.class);

        setRecordRefClass(RecordRef.class);

    }

    public static void main(String...args) throws Exception {
        new MetaDataModelGenImpl().run(args);
    }

}
