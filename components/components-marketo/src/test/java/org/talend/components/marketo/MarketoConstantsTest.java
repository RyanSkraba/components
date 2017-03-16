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
package org.talend.components.marketo;

import static org.junit.Assert.assertEquals;

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
        assertEquals("INT", ref.getField("Id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("Id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("LONG", ref.getField("ActivityDateTime").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("ActivityDateTime").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'.000Z'",
                ref.getField("ActivityDateTime").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("STRING", ref.getField("ActivityType").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("MktgAssetName").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("MktPersonId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("Campaign").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testLeadActivitySchemaSOAP() throws Exception {
        Schema ref = MarketoConstants.getSOAPSchemaForGetLeadActivity();
        assertEquals("INT", ref.getField("Id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("Id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("LONG", ref.getField("ActivityDateTime").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("ActivityDateTime").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'.000Z'",
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
        assertEquals("STRING", ref.getField("ListKeyType").schema().getType().toString());
        assertEquals("true", ref.getField("ListKeyType").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("ListKeyValue").schema().getType().toString());
        assertEquals("true", ref.getField("ListKeyValue").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("LeadKeyType").schema().getType().toString());
        assertEquals("true", ref.getField("LeadKeyType").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", ref.getField("LeadKeyValue").schema().getType().toString());
        assertEquals("true", ref.getField("LeadKeyValue").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
    }

    @Test
    public void testSyncLeadOutputSchemaSOAP() throws Exception {
        Schema ref = MarketoConstants.getSOAPOuputSchemaForSyncLead();
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
        assertEquals("INT", ref.getField("ListId").schema().getType().toString());
        assertEquals("true", ref.getField("ListId").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("INT", ref.getField("LeadId").schema().getType().toString());
        assertEquals("true", ref.getField("LeadId").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
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
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", ref.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
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
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", ref.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));

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
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", ref.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
    }

    @Test
    public void testNGetLeadChangesSchemaREST() throws Exception {
        Schema ref = MarketoConstants.getRESTSchemaForGetLeadChanges();
        assertEquals("INT", ref.getField("id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("INT", ref.getField("leadId").schema().getTypes().get(0).getType().toString());
        assertEquals("LONG", ref.getField("activityDate").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("activityDate").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("activityDate").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("INT", ref.getField("activityTypeId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("activityTypeValue").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("fields").schema().getTypes().get(0).getType().toString());
    }

    @Test
    public void testgGetLeadActivitySchemaREST() throws Exception {
        Schema ref = MarketoConstants.getRESTSchemaForGetLeadActivity();
        assertEquals("INT", ref.getField("id").schema().getTypes().get(0).getType().toString());
        assertEquals("true", ref.getField("id").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("INT", ref.getField("leadId").schema().getTypes().get(0).getType().toString());
        assertEquals("LONG", ref.getField("activityDate").schema().getType().toString());
        assertEquals("java.util.Date", ref.getField("activityDate").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", ref.getField("activityDate").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("INT", ref.getField("activityTypeId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("activityTypeValue").schema().getTypes().get(0).getType().toString());
        assertEquals("INT", ref.getField("primaryAttributeValueId").schema().getTypes().get(0).getType().toString());
        assertEquals("STRING", ref.getField("primaryAttributeValue").schema().getTypes().get(0).getType().toString());
    }
}
