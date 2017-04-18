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

import static org.apache.avro.SchemaBuilder.record;

import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Common shared schemas and constants
 */
public class MarketoConstants {

    public static final String API_REST = "REST";

    public static final String API_SOAP = "SOAP";

    public static final String FIELD_ERROR_MSG = "ERROR_MSG";

    public static final String FIELD_STATUS = "Status";

    public static final String FIELD_SUCCESS = "Success";

    public static final String FIELD_LEAD_ID = "LeadId";

    public static final String FIELD_LIST_ID = "ListId";

    public static final String FIELD_LEAD_KEY_VALUE = "LeadKeyValue";

    public static final String FIELD_LEAD_KEY_TYPE = "LeadKeyType";

    public static final String FIELD_LIST_KEY_VALUE = "ListKeyValue";

    public static final String FIELD_LIST_KEY_TYPE = "ListKeyType";

    public static final String FIELD_MARKETO_GUID = "marketoGUID";

    public static final String FIELD_SEQ = "seq";

    public static final String FIELD_REASON = "reason";

    public static final String FIELD_CREATED_AT = "createdAt";

    public static final String FIELD_UPDATED_AT = "updatedAt";

    public static final String FIELD_DEDUPE_FIELDS = "dedupeFields";

    public static final String DATETIME_PATTERN_PARAM = "yyyy-MM-dd HH:mm:ss";

    public static final String DATETIME_PATTERN_REST = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String DATETIME_PATTERN_SOAP = "yyyy-MM-dd'T'HH:mm:ss'.000Z'";

    public static Schema getEmptySchema() {
        return SchemaBuilder.builder().record("empty").fields().endRecord();
    }

    /**
     * List Operations
     */
    /////////////////////////////////////////////////////////////////////////
    // REST
    /////////////////////////////////////////////////////////////////////////
    public static Schema getListOperationRESTSchema() {
        return SchemaBuilder.builder().record("REST").fields() //
                .name(FIELD_LIST_ID)//
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")//
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().intType().noDefault() //
                //
                .name(FIELD_LEAD_ID)//
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")//
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().intType().noDefault() //
                .endRecord();
    }

    /////////////////////////////////////////////////////////////////////////
    // SOAP
    /////////////////////////////////////////////////////////////////////////
    public static Schema getListOperationSOAPSchema() {
        return SchemaBuilder.builder().record("SOAP").fields() //
                .name(FIELD_LIST_KEY_TYPE)//
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")//
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().stringType().noDefault() //
                .name(FIELD_LIST_KEY_VALUE)//
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")//
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().stringType().noDefault() //
                .name(FIELD_LEAD_KEY_TYPE)//
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")//
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().stringType().noDefault() //
                .name(FIELD_LEAD_KEY_VALUE)//
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")//
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().stringType().noDefault() //
                .endRecord();
    }

    /**
     *
     * Custom Objects
     *
     */
    /**
     * Sample :
     * {"requestId":"1495b#15a46590a99","result":[{"name":"smartphone_c","displayName":"Smartphone","description":"Smartphone
     * possessed by lead","createdAt":"2017-02-15T16:06:35Z","updatedAt":"2017-02-15T16:06:36Z",
     * "idField":"marketoGUID","dedupeFields":["model"],"searchableFields":[["model"],["marketoGUID"],["customerId"]],
     * "relationships":[{"field":"customerId","type":"child","relatedTo":{"name":"Lead","field":"Id"}}],"fields":[{"name":"createdAt","displayName":"Created
     * At","dataType":"datetime","updateable":false},{"name":"marketoGUID","displayName":"Marketo
     * GUID","dataType":"string","length":36,"updateable":false},{"name":"updatedAt","displayName":"Updated
     * At","dataType":"datetime","updateable":false},{"name":"acquiredAt","displayName":"Acquired
     * at","dataType":"date","updateable":true},{"name":"brand","displayName":"Brand","dataType":"string","length":255,"updateable":true},{"name":"customerId","displayName":"Customer
     * Id","dataType":"integer","updateable":true},{"name":"model","displayName":"Model","dataType":"string","length":255,"updateable":true}]}],"success":true}
     */
    public static Schema getCustomObjectDescribeSchema() {
        return record("CustomObjectREST").fields()//
                .name("name").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault()//
                .name("displayName").type().nullable().stringType().noDefault()//
                .name("description").type().nullable().stringType().noDefault()//
                .name(FIELD_CREATED_AT)//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                .name(FIELD_UPDATED_AT)//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                .name("idField").type().nullable().stringType().noDefault()//
                .name(FIELD_DEDUPE_FIELDS).type().nullable().stringType().noDefault()// String []
                .name("searchableFields").type().nullable().stringType().noDefault()// String [] []
                .name("fields").type().nullable().stringType().noDefault()// ObjectField[]
                .name("relationships").type().nullable().stringType().noDefault()// ObjectRelation[]
                //
                .endRecord();
    }

    public static Schema getCustomObjectRecordSchema() {
        return record("CustomObjectRecordREST").fields()//
                .name(FIELD_MARKETO_GUID).prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault()//
                .name(FIELD_SEQ).type().intType().noDefault()//
                .name(FIELD_CREATED_AT) //
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                .name(FIELD_UPDATED_AT) //
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                // .name(FIELD_DEDUPE_FIELDS).type().nullable().stringType().noDefault()//
                //
                .endRecord();
    }

    public static Schema getCustomObjectSyncSchema() {
        return record("CustomObjectSyncREST").fields()//
                .name(FIELD_MARKETO_GUID).prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault()//
                .endRecord();
    }

    /**
     * Lead Schemas
     */

    public static Schema getSOAPOutputSchemaForSyncLead() {
        return SchemaBuilder.builder().record("syncLeadSOAP").fields() //
                .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("Email").type().nullable().stringType().noDefault() //
                .name("ForeignSysPersonId").type().nullable().stringType().noDefault() //
                .name("ForeignSysType").type().nullable().stringType().noDefault() //
                .name(FIELD_STATUS).prop(SchemaConstants.TALEND_FIELD_GENERATED, "true")
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().nullable().stringType().noDefault() //
                .endRecord();
    }

    public static Schema getSOAPOutputSchemaForSyncMultipleLeads() {
        return SchemaBuilder.builder().record("syncLeadSOAP").fields() //
                .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("Email").type().nullable().stringType().noDefault() //
                .name("ForeignSysPersonId").type().nullable().stringType().noDefault() //
                .name("ForeignSysType").type().nullable().stringType().noDefault() //
                .name(FIELD_STATUS).prop(SchemaConstants.TALEND_FIELD_GENERATED, "true")
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().nullable().stringType().noDefault() //
                .name(FIELD_ERROR_MSG).prop(SchemaConstants.TALEND_FIELD_GENERATED, "true")
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().nullable().stringType().noDefault() //
                .endRecord();
    }

    public static Schema getRESTOutputSchemaForSyncLead() {
        return SchemaBuilder.builder().record("syncLeadREST").fields() //
                .name("id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("email").type().nullable().stringType().noDefault() //
                .name("firstName").type().nullable().stringType().noDefault() //
                .name("lastName").type().nullable().stringType().noDefault() //
                .name(FIELD_STATUS).prop(SchemaConstants.TALEND_FIELD_GENERATED, "true")
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().nullable().stringType().noDefault() //
                .endRecord();
    }

    public static Schema getRESTOutputSchemaForSyncMultipleLeads() {
        return SchemaBuilder.builder().record("syncLeadREST").fields() //
                .name("id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("email").type().nullable().stringType().noDefault() //
                .name("firstName").type().nullable().stringType().noDefault() //
                .name("lastName").type().nullable().stringType().noDefault() //
                .name(FIELD_STATUS).prop(SchemaConstants.TALEND_FIELD_GENERATED, "true")
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().nullable().stringType().noDefault() //
                .name(FIELD_ERROR_MSG).prop(SchemaConstants.TALEND_FIELD_GENERATED, "true")
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().nullable().stringType().noDefault() //
                .endRecord();
    }

    /////////////////////////////////////////////////////////////////////////
    // REST
    /////////////////////////////////////////////////////////////////////////
    public static Schema getRESTSchemaForGetLeadOrGetMultipleLeads() {
        return SchemaBuilder.builder().record("getLeadOrGetMultipleLeadsREST").fields() //
                .name("id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("email").type().nullable().stringType().noDefault() //
                .name("firstName").type().nullable().stringType().noDefault() //
                .name("lastName").type().nullable().stringType().noDefault() //
                .name(FIELD_CREATED_AT)//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                .name(FIELD_UPDATED_AT)//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                .endRecord();
    }

    public static Schema getRESTSchemaForGetLeadChanges() {
        return SchemaBuilder.builder().record("getLeadChangesREST").fields() //
                .name("id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("leadId").type().nullable().intType().noDefault() //
                .name("activityDate")//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                .name("activityTypeId").type().nullable().intType().noDefault() //
                .name("activityTypeValue").type().nullable().stringType().noDefault() //
                .name("fields").type().nullable().stringType().noDefault() //
                .endRecord();
    }

    public static Schema getRESTSchemaForGetLeadActivity() {
        return SchemaBuilder.builder().record("getLeadActivityREST").fields() //
                .name("id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("leadId").type().nullable().intType().noDefault() //
                .name("activityDate")//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._date()).noDefault()//
                .name("activityTypeId").type().nullable().intType().noDefault() //
                .name("activityTypeValue").type().nullable().stringType().noDefault() //
                .name("primaryAttributeValueId").type().nullable().intType().noDefault() //
                .name("primaryAttributeValue").type().nullable().stringType().noDefault() //
                .endRecord();
    }

    public static Schema getDeleteLeadsSchema() {
        return SchemaBuilder.builder().record("deleteLeads").fields() //
                .name("leadId").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .endRecord();
    }

    //////////////////////////////////////////////////////////////////////////
    // SOAP
    /////////////////////////////////////////////////////////////////////////
    public static Schema getSOAPSchemaForGetLeadOrGetMultipleLeads() {
        return SchemaBuilder.builder().record("getLeadOrGetMultipleLeadsSOAP").fields() //
                .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("Email").type().nullable().stringType().noDefault() //
                .name("ForeignSysPersonId").type().nullable().stringType().noDefault() //
                .name("ForeignSysType").type().nullable().stringType().noDefault() //
                .endRecord();
    }

    public static Schema getSOAPSchemaForGetLeadChanges() {
        return SchemaBuilder.builder().record("getLeadChangesSOAP").fields() //
                .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("ActivityDateTime")//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_SOAP)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")//
                .type(AvroUtils._date()).noDefault()//
                .name("ActivityType").type().nullable().stringType().noDefault() //
                .name("MktgAssetName").type().nullable().stringType().noDefault() //
                .name("MktPersonId").type().nullable().stringType().noDefault() //
                .name("Campaign").type().nullable().stringType().noDefault() //
                .endRecord();
    }

    public static Schema getSOAPSchemaForGetLeadActivity() {
        return SchemaBuilder.builder().record("getLeadActivitySOAP").fields() //
                .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("ActivityDateTime")//
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_SOAP)
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")//
                .type(AvroUtils._date()).noDefault()//
                .name("ActivityType").type().nullable().stringType().noDefault() //
                .name("MktgAssetName").type().nullable().stringType().noDefault() //
                .name("MktPersonId").type().nullable().stringType().noDefault() //
                .name("Campaign").type().nullable().stringType().noDefault() //
                .name("ForeignSysId").type().nullable().stringType().noDefault() //
                .name("PersonName").type().nullable().stringType().noDefault() //
                .name("OrgName").type().nullable().stringType().noDefault() //
                .name("ForeignSysOrgId").type().nullable().stringType().noDefault() //
                .endRecord();
    }

    /**
     * Bulk Operations
     */
    public static Schema getBulkImportLeadSchema() {
        Schema s = SchemaBuilder.builder().record("bulkImportLead").fields() //
                .name("batchId").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("importId").type().nullable().stringType().noDefault() //
                .name("message").type().nullable().stringType().noDefault() //
                .name("numOfLeadsProcessed").type().nullable().intType().noDefault() //
                .name("numOfRowsFailed").type().nullable().intType().noDefault() //
                .name("numOfRowsWithWarning").type().nullable().intType().noDefault() //
                .name("status").type().nullable().stringType().noDefault() //
                .name("failuresLogFile").type().nullable().stringType().noDefault() //
                .name("warningsLogFile").type().nullable().stringType().noDefault() //
                .endRecord();
        s.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        return s;
    }

    public static Schema getBulkImportCustomObjectSchema() {
        Schema s = SchemaBuilder.builder().record("bulkImportCustomObject").fields() //
                .name("batchId").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().nullable().intType().noDefault() //
                .name("importTime").type().nullable().stringType().noDefault() //
                .name("message").type().nullable().stringType().noDefault() //
                .name("numOfObjectsProcessed").type().nullable().intType().noDefault() //
                .name("numOfRowsFailed").type().nullable().intType().noDefault() //
                .name("numOfRowsWithWarning").type().nullable().intType().noDefault() //
                .name("objectApiName").type().nullable().stringType().noDefault() //
                .name("operation").type().nullable().stringType().noDefault() //
                .name("status").type().nullable().stringType().noDefault() //
                .name("failuresLogFile").type().nullable().stringType().noDefault() //
                .name("warningsLogFile").type().nullable().stringType().noDefault() //
                .endRecord();
        s.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        return s;
    }

}
