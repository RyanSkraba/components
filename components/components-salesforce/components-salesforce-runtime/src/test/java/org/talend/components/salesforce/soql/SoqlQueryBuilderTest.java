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
}
