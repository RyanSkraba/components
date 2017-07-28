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
package org.talend.components.marketo.runtime;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.components.marketo.runtime.MarketoSourceOrSink.TALEND6_DYNAMIC_COLUMN_POSITION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Field.Order;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Before;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoRuntimeTestBase {

    public MarketoSourceOrSink sourceOrSink;

    public MarketoSource source;

    public MarketoSink sink;

    public MarketoRESTClient client;

    @Before
    public void setUp() throws Exception {

        client = mock(MarketoRESTClient.class);
        when(client.getApi()).thenReturn("REST");
        when(client.getMultipleLeads(any(TMarketoInputProperties.class), any(String.class)))
                .thenReturn(getLeadRecordResult(false));
        when(client.getAllLeadFields()).thenReturn(fakeAllLeadFields());

        sourceOrSink = mock(MarketoSourceOrSink.class);
        when(sourceOrSink.initialize(any(RuntimeContainer.class), any(ComponentProperties.class)))
                .thenReturn(ValidationResult.OK);
        when(sourceOrSink.validate(any(RuntimeContainer.class))).thenReturn(ValidationResult.OK);
        when(sourceOrSink.validateConnection(any(MarketoProvideConnectionProperties.class))).thenReturn(ValidationResult.OK);
        when(sourceOrSink.getClientService(any(RuntimeContainer.class))).thenReturn(client);

        sink = mock(MarketoSink.class);
        when(sink.validate(any(RuntimeContainer.class))).thenReturn(ValidationResult.OK);
        when(sink.validateConnection(any(MarketoProvideConnectionProperties.class))).thenReturn(new ValidationResult(Result.OK));
        when(sink.getClientService(any(RuntimeContainer.class))).thenReturn(client);
    }

    @After
    public void tearDown() throws Exception {
    }

    public static MarketoRecordResult getLeadRecordResult(boolean withRemainCount) {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setSuccess(true);
        IndexedRecord record1 = new GenericData.Record(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        IndexedRecord record2 = new GenericData.Record(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        record1.put(0, 12345);// id
        record1.put(1, "eg@test.com");// email
        record1.put(2, "Evanence");
        record1.put(3, "valerian");
        record1.put(4, new Date());
        record1.put(5, new Date());
        record2.put(0, 12346);// id
        record2.put(1, "eg2@test.com");// email
        record2.put(2, "Evanence2");
        record2.put(3, "valerian2");
        record2.put(4, new Date());
        record2.put(5, new Date());
        if (withRemainCount) {
            mkto.setRemainCount(2);
            mkto.setStreamPosition("streamposition");
        } else {
            mkto.setRemainCount(0);
        }
        mkto.setRecordCount(2);
        mkto.setRecords(Arrays.asList(record1, record2));
        return mkto;
    }

    public static MarketoRecordResult getFailedRecordResult(String api, String code, String message) {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setSuccess(false);
        mkto.setErrors(Arrays.asList(new MarketoError(api, code, message)));
        return mkto;
    }

    public MarketoSyncResult getFailedSyncResult(boolean withSyncRecord) {
        MarketoSyncResult mkto = new MarketoSyncResult();
        mkto.setSuccess(false);
        mkto.setErrors(Arrays.asList(new MarketoError("REST", "error")));
        if (withSyncRecord) {
            SyncStatus sts1 = new SyncStatus();
            sts1.setId(12345);
            sts1.setStatus("failed");
            sts1.setMarketoGUID("mktoGUID");
            sts1.setSeq(0);
            List<SyncStatus> stl = new ArrayList<>();
            stl.add(sts1);
            mkto.setRecords(stl);
        }

        return mkto;
    }

    public MarketoSyncResult getSuccessSyncResult(String status) {
        MarketoSyncResult mkto = new MarketoSyncResult();
        mkto.setSuccess(true);
        mkto.setRecordCount(1);
        SyncStatus sts1 = new SyncStatus();
        sts1.setId(12345);
        sts1.setStatus(status);
        sts1.setMarketoGUID("mktoGUID");
        sts1.setSeq(0);
        mkto.setRecords(Arrays.asList(sts1));

        return mkto;
    }

    public MarketoSyncResult getFailedSyncResult(String api, String code, String message) {
        MarketoSyncResult mkto = new MarketoSyncResult();
        mkto.setSuccess(false);
        mkto.setRecordCount(0);
        List<MarketoError> errors = new ArrayList<>();
        MarketoError error = new MarketoError(api, code, message);
        errors.add(error);
        mkto.setErrors(errors);

        return mkto;
    }

    public Schema getLeadDynamicSchema() {
        Schema schema = Schema.createRecord("dynamic", null, null, false);
        Field f1 = new Schema.Field("email", AvroUtils._string(), "", null, Order.ASCENDING);
        Field f2 = new Schema.Field("firstName", AvroUtils._string(), "", null, Order.ASCENDING);
        schema.setFields(Arrays.asList(f1, f2));
        schema.addProp(TALEND6_DYNAMIC_COLUMN_POSITION, 2);
        schema = AvroUtils.setIncludeAllFields(schema, true);
        return schema;
    }

    public Schema getFullDynamicSchema() {
        Schema emptySchema = Schema.createRecord("dynamic", null, null, false);
        emptySchema.setFields(new ArrayList<Field>());
        emptySchema = AvroUtils.setIncludeAllFields(emptySchema, true);
        return emptySchema;
    }

    public static List<Field> fakeAllLeadFields() {
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("id", Schema.create(Type.INT), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("email", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("accountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("linkedInId", Schema.create(Type.INT), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("sfdcAccountId", Schema.create(Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("company", Schema.create(Type.STRING), null, (Object) null);
        fields.add(field);

        return fields;
    }

}
