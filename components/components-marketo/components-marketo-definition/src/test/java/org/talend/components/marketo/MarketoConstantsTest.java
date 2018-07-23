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
package org.talend.components.marketo;

import static org.junit.Assert.*;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_REST;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_SOAP;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.daikon.avro.SchemaConstants;

public class MarketoConstantsTest {

    /*
     * SOAP
     */
    @Test
    public void testLeadSchemaSOAP() throws Exception {
        Schema ref = MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads();
        assertEquals("INT", ref.getField("Id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("Id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("Email").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("ForeignSysPersonId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("ForeignSysType").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testLeadChangesSchemaSOAP() throws Exception {
        Schema ref = MarketoConstants.getSOAPSchemaForGetLeadChanges();
        assertEquals("LONG", ref.getField("Id").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("marketoGUID").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("marketoGUID").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("LONG", ref.getField("ActivityDateTime").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("ActivityDateTime").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_SOAP,
                ref.getField("ActivityDateTime").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("STRING", ref.getField("ActivityType").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("MktgAssetName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("MktPersonId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("Campaign").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testLeadActivitySchemaSOAP() throws Exception {
        Schema ref = MarketoConstants.getSOAPSchemaForGetLeadActivity();
        assertEquals("LONG", ref.getField("Id").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("marketoGUID").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("marketoGUID").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("LONG", ref.getField("ActivityDateTime").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("ActivityDateTime").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_SOAP,
                ref.getField("ActivityDateTime").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("STRING", ref.getField("ActivityType").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("MktgAssetName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("MktPersonId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("Campaign").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("ForeignSysId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("PersonName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("OrgName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("ForeignSysOrgId").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testListOperationSchemaSOAP() throws Exception {
        Schema ref = MarketoConstants.getListOperationSOAPSchema();
        assertEquals("STRING", ref.getField("ListKeyType").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("ListKeyValue").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("LeadKeyType").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("LeadKeyValue").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testSyncLeadOutputSchemaSOAP() throws Exception {
        Schema ref = MarketoConstants.getSOAPOutputSchemaForSyncLead();
        assertEquals("INT", ref.getField("Id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("Id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("Email").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("ForeignSysPersonId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("ForeignSysType").schema().getTypes().get(0).getType().toString());
    }

    /*
     * REST
     */
    @Test
    public void testListOperationSchemaREST() throws Exception {
        Schema ref = MarketoConstants.getListOperationRESTSchema();
        assertEquals("INT", ref.getField("ListId").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("LeadId").schema().getTypes().get(0).getType().toString());
    }

    // Custom Objects
    @Test
    public void testCustomObjectsSyncSchema() throws Exception {
        Schema ref = MarketoConstants.getCustomObjectSyncSchema();
        assertEquals("STRING", ref.getField("marketoGUID").schema().getType().toString());
        assertEquals("true", ref.getField("marketoGUID").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
    }

    @Test
    public void testCustomObjectDescribeSchema() throws Exception {
        Schema ref = MarketoConstants.getCustomObjectDescribeSchema();
        assertEquals("STRING", ref.getField("name").schema().getType().toString());
        assertEquals("true", ref.getField("name").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("displayName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("description").schema().getTypes().get(0).getType().toString());
        assertEquals("LONG", ref.getField("createdAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("createdAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", ref.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("STRING", ref.getField("idField").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("dedupeFields").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("searchableFields").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("fields").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("relationships").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testCustomObjectRecordSchema() throws Exception {
        Schema ref = MarketoConstants.getCustomObjectRecordSchema();
        assertEquals("STRING", ref.getField("marketoGUID").schema().getType().toString());
        assertEquals("true", ref.getField("marketoGUID").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("INT", ref.getField("seq").schema().getType().toString());
        assertEquals("LONG", ref.getField("createdAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("createdAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", ref.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));

    }

    @Test
    public void testOutputSchemaForSyncLeadREST() throws Exception {
        Schema ref = MarketoConstants.getRESTOutputSchemaForSyncLead();
        assertEquals("INT", ref.getField("id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("email").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("firstName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("lastName").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testGetLeadOrGetMultipleLeadsSchemaREST() throws Exception {
        Schema ref = MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads();
        assertEquals("INT", ref.getField("id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("email").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("firstName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("lastName").schema().getTypes().get(0).getType().toString());
        assertEquals("LONG", ref.getField("createdAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("createdAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", ref.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
    }

    @Test
    public void testGetLeadChangesSchemaREST() throws Exception {
        Schema ref = MarketoConstants.getRESTSchemaForGetLeadChanges();
        assertEquals("INT", ref.getField("id").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("marketoGUID").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("marketoGUID").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("INT", ref.getField("leadId").schema().getTypes().get(0).getType().toString());
        assertEquals("LONG", ref.getField("activityDate").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("activityDate").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST,
                ref.getField("activityDate").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("INT", ref.getField("activityTypeId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("activityTypeValue").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("fields").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testGetLeadActivitySchemaREST() throws Exception {
        Schema ref = MarketoConstants.getRESTSchemaForGetLeadActivity();
        assertEquals("INT", ref.getField("id").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("marketoGUID").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("marketoGUID").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("INT", ref.getField("leadId").schema().getTypes().get(0).getType().toString());
        assertEquals("LONG", ref.getField("activityDate").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("activityDate").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST,
                ref.getField("activityDate").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("INT", ref.getField("activityTypeId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("activityTypeValue").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("primaryAttributeValueId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("primaryAttributeValue").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testGetBulkImportLeadSchema() throws Exception {
        Schema ref = MarketoConstants.getBulkImportLeadSchema();
        assertEquals("true", ref.getProp(SchemaConstants.TALEND_IS_LOCKED));
        assertEquals("INT", ref.getField("batchId").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("batchId").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("importId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("message").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("numOfLeadsProcessed").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("numOfRowsFailed").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("numOfRowsWithWarning").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("status").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("failuresLogFile").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("warningsLogFile").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testGetBulkImportCustomObjectSchema() throws Exception {
        Schema ref = MarketoConstants.getBulkImportCustomObjectSchema();
        assertEquals("true", ref.getProp(SchemaConstants.TALEND_IS_LOCKED));
        assertEquals("INT", ref.getField("batchId").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("batchId").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("importTime").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("message").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("numOfObjectsProcessed").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("numOfRowsFailed").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("numOfRowsWithWarning").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("objectApiName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("operation").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("status").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("failuresLogFile").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("warningsLogFile").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testGetCampaignSchema() throws Exception {
        Schema ref = MarketoConstants.getCampaignSchema();
        assertEquals("INT", ref.getField("id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("name").schema().getTypes().get(0).getType().toString());
        assertEquals("BOOLEAN", ref.getField("active").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("description").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("programId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("programName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("type").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("workspaceName").schema().getTypes().get(0).getType().toString());
        assertEquals("LONG", ref.getField("createdAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("createdAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", ref.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, ref.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
    }

    @Test
    public void testScheduleCampaignSchema() throws Exception {
        Schema ref = MarketoConstants.scheduleCampaignSchema();
        assertEquals("INT", ref.getField("campaignId").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("campaignId").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("Status").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testTriggerCampaignSchema() throws Exception {
        Schema ref = MarketoConstants.triggerCampaignSchema();
        assertEquals("INT", ref.getField("leadId").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("leadId").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
    }

    @Test
    public void testTriggerCampaignSchemaFlow() throws Exception {
        Schema ref = MarketoConstants.triggerCampaignSchemaFlow();
        assertEquals("INT", ref.getField("campaignId").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("campaignId").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
    }

}
