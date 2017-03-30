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

import static java.lang.System.getProperty;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OperationType;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OutputOperation;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.RESTLookupFields;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class MarketoBaseTestIT {

    @Inject
    DefinitionRegistryService definitionRegistryService;

    public DefinitionRegistryService getDefinitionRegistry() {
        return definitionRegistryService;
    }

    public static String ENDPOINT_SOAP;

    public static String USERID_SOAP;

    public static String SECRETKEY_SOAP;

    public static String ENDPOINT_REST;

    public static String USERID_REST;

    public static String SECRETKEY_REST;

    public static final String DATE_FARFARAWAY = "2016-01-01 00:00:00";

    public static final String DATE_OLDEST_CREATE = "2016-12-01 00:00:00";

    public static final String DATE_LATEST_CREATE = "2016-12-31 00:00:00";

    public static final String DATE_OLDEST_UPDATE = "2016-12-01 00:00:00";

    public static final String DATE_LATEST_UPDATE = "2017-01-31 00:00:00";

    public static final String EMAIL_UNDX00 = "undx00@undx.net";

    public static final String EMAIL_LEAD_MANY_INFOS = "mark.dalton@gmail.com";

    public static final String EMAIL_LEAD_TEST = "undx@undx.net";

    public static final String EMAIL_INEXISTANT = "toto.titi@tata.com";

    public static final Integer COMMON_LINKEDIN_ID = 212555666;

    public static final String COMMON_SFDC_ACCOUNT_ID = "NHHGHG123456Wsfdc";

    public static final String ENDPOINT_URL_INEXISTANT = "https://nonexistent.domain.go/soap/mktows/2_9";

    public static final String UNDX_TEST_LIST_SMALL = "undx_test_list_small";

    public static final Integer UNDX_TEST_LIST_SMALL_ID = 29913;

    public static final String EMAIL_PREFIX = "undx";

    public static final String EMAIL_DOMAIN = "@undx.net";

    public static final String LIST_KEY_TYPE_MKTOLISTNAME = "MKTOLISTNAME";

    public static final String LIST_KEY_TYPE_MKTOSALESUSERID = "MKTOSALESUSERID";

    public static final String LIST_KEY_TYPE_SFDCLEADOWNERID = "SFDCLEADOWNERID";

    static {
        ENDPOINT_SOAP = getProperty("marketo.soap.endpoint");
        USERID_SOAP = getProperty("marketo.soap.userid");
        SECRETKEY_SOAP = getProperty("marketo.soap.secretkey");
        ENDPOINT_REST = getProperty("marketo.rest.endpoint");
        USERID_REST = getProperty("marketo.rest.userid");
        SECRETKEY_REST = getProperty("marketo.rest.secretkey");
    }

    protected static List<Integer> createdLeads = new ArrayList<>();

    protected static MarketoRESTClient datasetsClient;

    protected transient static final Logger LOG = LoggerFactory.getLogger(MarketoBaseTestIT.class);

    public static TMarketoOutputProperties initClient() throws Exception {
        TMarketoOutputProperties p = new TMarketoOutputProperties("test");
        p.setupProperties();
        p.connection.setupProperties();
        p.connection.endpoint.setValue(ENDPOINT_REST);
        p.connection.clientAccessId.setValue(USERID_REST);
        p.connection.secretKey.setValue(SECRETKEY_REST);
        p.connection.apiMode.setValue(REST);
        p.connection.setupLayout();
        p.schemaInput.setupProperties();
        p.schemaInput.setupLayout();
        p.setupLayout();
        p.batchSize.setValue(300);
        p.outputOperation.setValue(OutputOperation.syncLead);
        p.operationType.setValue(OperationType.createOrUpdate);
        p.lookupField.setValue(RESTLookupFields.email);
        p.deDupeEnabled.setValue(false);
        p.afterApiMode();
        p.updateSchemaRelated();

        MarketoSource source = new MarketoSource();
        source.initialize(null, p);
        datasetsClient = (MarketoRESTClient) source.getClientService(null);
        return p;
    }

    public static void createDatasets(int numberOfLeads) throws Exception {
        LOG.info("Creating test leads");
        TMarketoOutputProperties p = initClient();
        if (numberOfLeads < 5) {
            numberOfLeads = 5;
        }
        // create leads for tests
        // test attributes
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("accountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("linkedInId", Schema.create(Type.INT), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("sfdcAccountId", Schema.create(Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("company", Schema.create(Type.STRING), null, (Object) null);
        fields.add(field);

        Schema s = p.newSchema(p.schemaInput.schema.getValue(), "leadAttribute", fields);
        p.schemaInput.schema.setValue(s);
        p.updateSchemaRelated();
        //
        List<IndexedRecord> leads = new ArrayList<>();
        //
        for (int i = 0; i < numberOfLeads; i++) {
            IndexedRecord record = new GenericData.Record(s);
            record.put(0, null);
            record.put(1, String.format("%s%02d%s", EMAIL_PREFIX, i, EMAIL_DOMAIN));
            record.put(2, String.format("firstName%02d", i));
            record.put(3, String.format("lastName%02d", i));
            record.put(4, String.format("Account Type %02d", i));
            record.put(5, COMMON_LINKEDIN_ID); // all the same linkedInId.
            record.put(6, COMMON_SFDC_ACCOUNT_ID); // all the same SalesforceId.
            record.put(7, "Retail-Dev"); // all the same SalesforceId.
            leads.add(record);
        }
        ///
        MarketoSyncResult result = datasetsClient.syncMultipleLeads(p, leads);
        LOG.debug("result = {}.", result);
        List<SyncStatus> changes = result.getRecords();
        assertTrue(changes.size() > 0);
        for (SyncStatus r : changes) {
            assertNotNull(r);
            assertNotNull(r.getId());
            createdLeads.add(r.getId());
        }
        LOG.info("Created {} leads for tests. Now adding 5 leads to {} list.", createdLeads.size(), UNDX_TEST_LIST_SMALL);
        // adding 5 members to test list
        ListOperationParameters parms = new ListOperationParameters();
        parms.setApiMode(REST.name());
        parms.setListId(UNDX_TEST_LIST_SMALL_ID);
        parms.setLeadIds(new Integer[] { createdLeads.get(0), createdLeads.get(1), createdLeads.get(2), createdLeads.get(3),
                createdLeads.get(4) });
        //
        result = datasetsClient.addToList(parms);
        LOG.debug("result = {}.", result);
        LOG.info("Test Dataset created successfully.");
    }

    public static void cleanupDatasets() {
        LOG.info("Deleting test leads");
        datasetsClient.deleteLeads(createdLeads.toArray(new Integer[] {}));
        LOG.info("Deleted {} leads.", createdLeads.size());
        createdLeads.clear();
    }

}
