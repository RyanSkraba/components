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
package org.talend.components.salesforce.soql;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit-tests for {@link SoqlQueryBuilder} class
 */
public class SoqlQueryBuilderTest {

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name
     */
    @Test
    public void testBuildSoqlQuerySimple() {
        String expected = "\"SELECT Id, Name, BillingCity FROM Account\"";

        Schema schema = SchemaBuilder.record("Result").fields().name("Id").type().stringType().noDefault().name("Name").type()
                .stringType().noDefault().name("BillingCity").type().stringType().noDefault().endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "Account").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * child-to-parent relationship
     */
    @Test
    public void testBuildSoqlQueryChildToParent() {
        String expected = "\"SELECT Name, Account.Name, Account.Owner.Name FROM Contact\"";

        Schema schema = SchemaBuilder.record("Result").fields().name("Name").type().stringType().noDefault().name("Account_Name")
                .type().stringType().noDefault().name("Account_Owner_Name").type().stringType().noDefault().endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "Contact").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * parent-to-child relationship
     */
    @Test
    public void testBuildSoqlQueryParentToChild() {
        String expected = "\"SELECT Name, (SELECT LastName FROM Contacts) FROM Account\"";

        Schema schema = SchemaBuilder.record("Result").fields().name("Name").type().stringType().noDefault()
                .name("Contacts_records_LastName").type().stringType().noDefault().endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "Account").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * parent-to-child relationship in case of three-level entities linking
     */
    @Test
    public void testBuildSoqlQueryParentToChildDepth() {
        String expected = "\"SELECT Name, (SELECT LastName, Account.Owner.Name FROM Contacts) FROM Account\"";

        Schema schema = SchemaBuilder.record("Result").fields().name("Name").type().stringType().noDefault()
                .name("Contacts_records_LastName").type().stringType().noDefault().name("Contacts_records_Account_Owner_Name")
                .type().stringType().noDefault().endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "Account").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * complex relationship
     */
    @Test
    public void testBuildSoqlQueryComplexRelationship() {
        String expected = "\"SELECT Id, Name, (SELECT Quantity, ListPrice, PricebookEntry.UnitPrice, PricebookEntry.Name FROM OpportunityLineItems) FROM Opportunity\"";

        Schema schema = SchemaBuilder.record("Result").fields().name("Id").type().stringType().noDefault().name("Name").type()
                .stringType().noDefault().name("OpportunityLineItems_records_Quantity").type().stringType().noDefault()
                .name("OpportunityLineItems_records_ListPrice").type().stringType().noDefault()
                .name("OpportunityLineItems_records_PricebookEntry_UnitPrice").type().stringType().noDefault()
                .name("OpportunityLineItems_records_PricebookEntry_Name").type().stringType().noDefault().endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "Opportunity").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * custom field
     */
    @Test
    public void testBuildSoqlQueryCustomField() {
        String expected = "\"SELECT Id, SLAExpirationDate__c FROM Account\"";

        Schema schema = SchemaBuilder.record("Result").fields().name("Id").type().stringType().noDefault()
                .name("SLAExpirationDate__c").type().stringType().noDefault().endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "Account").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * custom table
     */
    @Test
    public void testBuildSoqlQueryWithCustomTable() {
        String expected = "\"SELECT Id, SLAExpirationDate__c FROM talendCustom__c\"";

        Schema schema = SchemaBuilder.record("Result").fields().requiredString("Id").requiredString("SLAExpirationDate__c")
                .endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "talendCustom__c").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * custom fields/table and relation child to parent
     */
    @Test
    public void testBuildSoqlQueryWithCustomTablesChildToParent() {
        String expected =
                "\"SELECT talendCustom__c.Name, talendCustom__c.customName__c, talendContact__c.Name, talendContact__c.Account.Name FROM talendCustom__c\"";

        Schema schema = SchemaBuilder.record("Result").fields()
                .requiredString("talendCustom__c_Name")
                .requiredString("talendCustom__c_customName__c")
                .requiredString("talendContact__c_Name")
                .requiredString("talendContact__c_Account_Name")
                .endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "talendCustom__c").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * custom fields/table and relation parent to child
     */
    @Test
    public void testBuildSoqlQueryWithCustomTablesParentToChild() {
        String expected = "\"SELECT Name, Contact.title__c, (SELECT custom.lastName, custom.name__c, talend.account__c.custom.lastName__c, talend.account__c.Age FROM talend.custom__r) FROM contact__c\"";

        Schema schema = SchemaBuilder.record("Result").fields()
                .requiredString("Name")
                .requiredString("talend_custom__r_records_custom_lastName")
                .requiredString("Contact_title__c")
                .requiredString("talend_custom__r_records_custom_name__c")
                .requiredString("talend_custom__r_records_talend_account__c_custom_lastName__c")
                .requiredString("talend_custom__r_records_talend_account__c_Age")
                .endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "contact__c").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }

    /**
     * Checks {@link SoqlQueryBuilder#buildSoqlQuery()} returns SOQL query according to schema and entity name with
     * custom fields, and complex table name with relation parent to child
     */
    @Test
    public void testBuildSoqlQueryWithComplexChildTableNameParentToChild() {
        String expected = "\"SELECT Name, Contact.title__c, (SELECT custom.lastName, talend.account__c.Age FROM talend.contact__c.Persons) FROM contact__c\"";

        Schema schema = SchemaBuilder.record("Result").fields()
                .requiredString("Name")
                .requiredString("talend_contact__c_Persons_records_custom_lastName")
                .requiredString("Contact_title__c")
                .requiredString("talend_contact__c_Persons_records_talend_account__c_Age")
                .endRecord();

        String queryFromBuilder = new SoqlQueryBuilder(schema, "contact__c").buildSoqlQuery();

        Assert.assertEquals(expected, queryFromBuilder);
    }
}
