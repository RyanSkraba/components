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
package org.talend.components.marketo.runtime.client;

import static com.marketo.mktows.ActivityType.fromValue;
import static com.marketo.mktows.LeadKeyRef.valueOf;
import static com.marketo.mktows.ListOperationType.ISMEMBEROFLIST;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static javax.crypto.Mac.getInstance;
import static javax.xml.datatype.DatatypeFactory.newInstance;
import static org.apache.avro.Schema.Field;
import static org.apache.avro.generic.GenericData.Record;
import static org.apache.commons.codec.binary.Hex.encodeHex;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_PARAM;
import static org.talend.components.marketo.MarketoConstants.FIELD_ERROR_MSG;
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LastUpdateAtSelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LeadKeySelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.StaticListSelector;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam.STATIC_LIST_NAME;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

import com.marketo.mktows.ActivityRecord;
import com.marketo.mktows.ActivityTypeFilter;
import com.marketo.mktows.ArrayOfActivityType;
import com.marketo.mktows.ArrayOfAttribute;
import com.marketo.mktows.ArrayOfLeadKey;
import com.marketo.mktows.ArrayOfLeadRecord;
import com.marketo.mktows.ArrayOfString;
import com.marketo.mktows.Attribute;
import com.marketo.mktows.AuthenticationHeader;
import com.marketo.mktows.ForeignSysType;
import com.marketo.mktows.LastUpdateAtSelector;
import com.marketo.mktows.LeadChangeRecord;
import com.marketo.mktows.LeadKey;
import com.marketo.mktows.LeadKeyRef;
import com.marketo.mktows.LeadKeySelector;
import com.marketo.mktows.LeadRecord;
import com.marketo.mktows.LeadStatus;
import com.marketo.mktows.ListKey;
import com.marketo.mktows.ListKeyType;
import com.marketo.mktows.ListOperationType;
import com.marketo.mktows.MktMktowsApiService;
import com.marketo.mktows.MktowsContextHeader;
import com.marketo.mktows.MktowsPort;
import com.marketo.mktows.ObjectFactory;
import com.marketo.mktows.ParamsGetLead;
import com.marketo.mktows.ParamsGetLeadActivity;
import com.marketo.mktows.ParamsGetLeadChanges;
import com.marketo.mktows.ParamsGetMultipleLeads;
import com.marketo.mktows.ParamsListMObjects;
import com.marketo.mktows.ParamsListOperation;
import com.marketo.mktows.ParamsSyncLead;
import com.marketo.mktows.ParamsSyncMultipleLeads;
import com.marketo.mktows.StaticListSelector;
import com.marketo.mktows.StreamPosition;
import com.marketo.mktows.SuccessGetLead;
import com.marketo.mktows.SuccessGetLeadActivity;
import com.marketo.mktows.SuccessGetLeadChanges;
import com.marketo.mktows.SuccessGetMultipleLeads;
import com.marketo.mktows.SuccessListOperation;
import com.marketo.mktows.SuccessSyncLead;
import com.marketo.mktows.SuccessSyncMultipleLeads;

public class MarketoSOAPClient extends MarketoClient {

    private transient static final Logger LOG = getLogger(MarketoSOAPClient.class);

    public static final String SOAP = "SOAP";

    public static final String FIELD_ID = "Id";

    public static final String FIELD_EMAIL = "Email";

    public static final String FIELD_FOREIGN_SYS_PERSON_ID = "ForeignSysPersonId";

    public static final String FIELD_FOREIGN_SYS_TYPE = "ForeignSysType";

    public static final String FIELD_ACTIVITY_DATE_TIME = "ActivityDateTime";

    public static final String FIELD_ACTIVITY_TYPE = "ActivityType";

    public static final String FIELD_MKTG_ASSET_NAME = "MktgAssetName";

    public static final String FIELD_MKT_PERSON_ID = "MktPersonId";

    public static final String FIELD_CAMPAIGN = "Campaign";

    public static final String FIELD_FOREIGN_SYS_ID = "ForeignSysId";

    public static final String FIELD_PERSON_NAME = "PersonName";

    public static final String FIELD_ORG_NAME = "OrgName";

    public static final String FIELD_FOREIGN_SYS_ORG_ID = "ForeignSysOrgId";

    private MktowsPort port;

    private AuthenticationHeader header;

    private ObjectFactory objectFactory;

    public MarketoSOAPClient(TMarketoConnectionProperties connection) throws MarketoException {
        try {
            LOG.debug("Marketo SOAP Client initialization.");
            objectFactory = new ObjectFactory();
            endpoint = connection.endpoint.getValue();
            userId = connection.clientAccessId.getValue();
            secretKey = connection.secretKey.getValue();

            URL marketoSoapEndPoint = null;
            marketoSoapEndPoint = new URL(endpoint + "?WSDL");

            QName serviceName = new QName("http://www.marketo.com/mktows/", "MktMktowsApiService");
            MktMktowsApiService service = new MktMktowsApiService(marketoSoapEndPoint, serviceName);
            port = service.getMktowsApiSoapPort();
            // Create Signature
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            String text = df.format(new Date());
            String requestTimestamp = text.substring(0, 22) + ":" + text.substring(22);
            String encryptString = requestTimestamp + userId;
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = getInstance("HmacSHA1");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(encryptString.getBytes());
            char[] hexChars = encodeHex(rawHmac);
            String signature = new String(hexChars);
            // Set Authentication Header
            header = new AuthenticationHeader();
            header.setMktowsUserId(userId);
            header.setRequestTimestamp(requestTimestamp);
            header.setRequestSignature(signature);
            // bug/TDI-38439_MarketoWizardConnection : make a dummy call to check auth and not just URL.
            port.listMObjects(new ParamsListMObjects(), header);
        } catch (MalformedURLException | NoSuchAlgorithmException | InvalidKeyException e) {
            // LOG.error("Client connection error : {}.", e.getMessage());
            throw new MarketoException(SOAP, e.getMessage());
        } catch (WebServiceException e) {
            // TODO manage connection reset and socket closed error with timeout and retry properties.
            // String socket_close = "Socket closed";
            // String connection_reset = "Connection reset";
            throw new MarketoException(SOAP, e.getMessage());
        }
    }

    @Override
    public String getApi() {
        return SOAP;
    }

    public String toString() {
        return format("Marketo SOAP API Client [%s].", endpoint);
    }

    public List<IndexedRecord> convertLeadRecords(List<LeadRecord> recordList, Schema schema, Map<String, String> mappings) {
        List<IndexedRecord> results = new ArrayList<>();
        for (LeadRecord input : recordList) {
            IndexedRecord record = new Record(schema);
            for (Field f : schema.getFields()) {
                // find matching marketo column name
                String col = mappings.get(f.name());
                if (col == null) {
                    LOG.warn("[converLeadRecord] Couldn't find mapping for column {}.", f.name());
                    continue;
                }
                if (col.equals(FIELD_ID)) {
                    record.put(f.pos(), input.getId().getValue());
                } else if (col.equals(FIELD_EMAIL)) {
                    record.put(f.pos(), input.getEmail().getValue());
                } else if (col.equals(FIELD_FOREIGN_SYS_PERSON_ID)) {
                    record.put(f.pos(), input.getForeignSysPersonId().getValue());
                } else if (col.equals(FIELD_FOREIGN_SYS_TYPE)) {
                    record.put(f.pos(), input.getForeignSysType().getValue());
                } else {
                    for (Attribute attr : input.getLeadAttributeList().getValue().getAttributes()) {
                        if (attr.getAttrName().equals(col)) {
                            record.put(f.pos(), attr.getAttrValue());
                        }
                    }
                }
            }
            results.add(record);
        }
        return results;
    }

    private List<IndexedRecord> convertLeadActivityRecords(List<ActivityRecord> activityRecords, Schema schema,
            Map<String, String> mappings) {
        List<IndexedRecord> results = new ArrayList<>();
        for (ActivityRecord input : activityRecords) {
            IndexedRecord record = new Record(schema);
            for (Field f : schema.getFields()) {
                // find matching marketo column name
                String col = mappings.get(f.name());
                if (col == null) {
                    LOG.warn("[convertLeadActivityRecords] Couldn't find mapping for column {}.", f.name());
                    continue;
                }
                if (col.equals(FIELD_ID)) {
                    record.put(f.pos(), input.getId());
                } else if (col.equals(FIELD_ACTIVITY_DATE_TIME)) {
                    record.put(f.pos(), input.getActivityDateTime().toGregorianCalendar().getTime());
                    //
                } else if (col.equals(FIELD_ACTIVITY_TYPE)) {
                    record.put(f.pos(), input.getActivityType());
                } else if (col.equals(FIELD_MKTG_ASSET_NAME)) {
                    record.put(f.pos(), input.getMktgAssetName());
                } else if (col.equals(FIELD_MKT_PERSON_ID)) {
                    record.put(f.pos(), input.getMktPersonId());
                } else if (col.equals(FIELD_CAMPAIGN)) {
                    record.put(f.pos(), input.getCampaign().getValue());
                } else if (col.equals(FIELD_FOREIGN_SYS_ID)) {
                    record.put(f.pos(), input.getForeignSysId().getValue());
                } else if (col.equals(FIELD_PERSON_NAME)) {
                    record.put(f.pos(), input.getPersonName().getValue());
                } else if (col.equals(FIELD_ORG_NAME)) {
                    record.put(f.pos(), input.getOrgName().getValue());
                } else if (col.equals(FIELD_FOREIGN_SYS_ORG_ID)) {
                    record.put(f.pos(), input.getForeignSysOrgId().getValue());
                } else {
                    for (Attribute attr : input.getActivityAttributes().getValue().getAttributes()) {
                        if (attr.getAttrName().equals(col)) {
                            record.put(f.pos(), attr.getAttrValue());
                        }
                    }
                }
            }
            results.add(record);
        }

        return results;
    }

    private List<IndexedRecord> convertLeadChangeRecords(List<LeadChangeRecord> value, Schema schema,
            Map<String, String> mappings) {
        List<IndexedRecord> results = new ArrayList<>();
        for (LeadChangeRecord input : value) {
            IndexedRecord record = new Record(schema);
            for (Field f : schema.getFields()) {
                // find matching marketo column name
                String col = mappings.get(f.name());
                if (col == null) {
                    LOG.warn("[convertLeadChangeRecords] Couldn't find mapping for column {}.", f.name());
                    continue;
                }
                if (col.equals(FIELD_ID)) {
                    record.put(f.pos(), input.getId());
                } else if (col.equals(FIELD_ACTIVITY_DATE_TIME)) {
                    record.put(f.pos(), input.getActivityDateTime().toGregorianCalendar().getTime());
                } else if (col.equals(FIELD_ACTIVITY_TYPE)) {
                    record.put(f.pos(), input.getActivityType());
                } else if (col.equals(FIELD_MKTG_ASSET_NAME)) {
                    record.put(f.pos(), input.getMktgAssetName().getValue());
                } else if (col.equals(FIELD_MKT_PERSON_ID)) {
                    record.put(f.pos(), input.getMktPersonId());
                } else if (col.equals(FIELD_CAMPAIGN)) {
                    record.put(f.pos(), input.getCampaign());
                } else {
                    for (Attribute attr : input.getActivityAttributes().getValue().getAttributes()) {
                        if (attr.getAttrName().equals(col)) {
                            record.put(f.pos(), attr.getAttrValue());
                        }
                    }
                }
            }
            results.add(record);
        }
        return results;
    }

    @Override
    public MarketoRecordResult getLead(TMarketoInputProperties parameters, String offset) {
        LOG.debug("MarketoSOAPClient.getLead with selector:{} key:{} value:{}.", parameters.leadSelectorSOAP.getValue(),
                parameters.leadKeyTypeSOAP.getValue(), parameters.leadKeyValue.getValue());
        long start = currentTimeMillis();

        List<IndexedRecord> results = new ArrayList<>();
        String leadKeyType = parameters.leadKeyTypeSOAP.getValue().toString();
        String leadKeyValue = parameters.leadKeyValue.getValue();
        Schema schema = parameters.schemaInput.schema.getValue();
        Map<String, String> mappings = parameters.mappingInput.getNameMappingsForMarketo();
        // Create Request
        ParamsGetLead request = new ParamsGetLead();
        LeadKey key = new LeadKey();
        key.setKeyType(LeadKeyRef.valueOf(leadKeyType));
        key.setKeyValue(leadKeyValue);
        request.setLeadKey(key);
        //
        long tPrep = currentTimeMillis();

        SuccessGetLead result = null;
        MarketoRecordResult mkto = new MarketoRecordResult();
        try {
            result = port.getLead(request, header);
        } catch (Exception e) {
            LOG.error("Lead not found : {}.", e.getMessage());
            mkto.setSuccess(false);
            mkto.setRecordCount(0);
            mkto.setRemainCount(0);
            mkto.setErrors(Arrays.asList(new MarketoError(SOAP, e.getMessage())));
        }
        long tRequest = currentTimeMillis();

        if (result == null || !(result.getResult().getCount() > 0)) {
            LOG.debug("Request returned 0 matching lead.");
            mkto.setErrors(Arrays.asList(new MarketoError(SOAP, "No leads found.")));
            mkto.setSuccess(true);
        } else {
            int counted = result.getResult().getCount();
            results = convertLeadRecords(result.getResult().getLeadRecordList().getValue().getLeadRecords(), schema, mappings);

            long tConv = currentTimeMillis();

            LOG.info("Results : count = {}; remaing = {}; Stream Position = {}.", counted, 0, "");
            LOG.info("Timings : Preparation = {}ms; Request = {}ms; Convertion = {}ms.", (tPrep - start), (tRequest - tPrep),
                    (tConv - tRequest));
            mkto.setRecordCount(counted);
            mkto.setRemainCount(0);
            mkto.setSuccess(true);
            mkto.setRecords(results);
        }
        return mkto;
    }

    /**
     * In getMultipleLeadsJSON you have to add includeAttributes base fields like Email. Otherwise, they return null
     * from API. WTF ?!? It's like that...
     */
    @Override
    public MarketoRecordResult getMultipleLeads(TMarketoInputProperties parameters, String offset) {
        LOG.debug("MarketoSOAPClient.getMultipleLeadsJSON with {}", parameters.leadSelectorSOAP.getValue());
        long start = currentTimeMillis();
        List<IndexedRecord> results = new ArrayList<>();

        Schema schema = parameters.schemaInput.schema.getValue();
        Map<String, String> mappings = parameters.mappingInput.getNameMappingsForMarketo();
        int bSize = parameters.batchSize.getValue();
        //
        // Create Request
        //
        ParamsGetMultipleLeads request = new ParamsGetMultipleLeads();
        // LeadSelect
        //
        // Request Using LeadKey Selector
        //
        if (parameters.leadSelectorSOAP.getValue().equals(LeadKeySelector)) {
            LOG.info("LeadKeySelector - Key type:  {} with value : {}.", parameters.leadKeyTypeSOAP.getValue().toString(),
                    parameters.leadKeyValues.getValue());
            LeadKeySelector keySelector = new LeadKeySelector();
            keySelector.setKeyType(valueOf(parameters.leadKeyTypeSOAP.getValue().toString()));
            ArrayOfString aos = new ArrayOfString();
            String[] keys = parameters.leadKeyValues.getValue().split("(,|;|\\s)");
            for (String s : keys) {
                LOG.debug("Adding leadKeyValue : {}.", s);
                // if (!s.isEmpty())
                aos.getStringItems().add(s);
            }
            keySelector.setKeyValues(aos);
            request.setLeadSelector(keySelector);
        } else
        //
        // Request Using LastUpdateAtSelector
        //

        if (parameters.leadSelectorSOAP.getValue().equals(LastUpdateAtSelector)) {
            LOG.info("LastUpdateAtSelector - since {} to  {}.", parameters.oldestUpdateDate.getValue(),
                    parameters.latestUpdateDate.getValue());

            LastUpdateAtSelector leadSelector = new LastUpdateAtSelector();
            try {
                DatatypeFactory factory = newInstance();
                ObjectFactory objectFactory = new ObjectFactory();
                DateFormat df = new SimpleDateFormat(DATETIME_PATTERN_PARAM);
                Date oldest = df.parse(parameters.oldestUpdateDate.getValue());
                Date latest = df.parse(parameters.latestUpdateDate.getValue());
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(latest);
                JAXBElement<XMLGregorianCalendar> until = objectFactory
                        .createLastUpdateAtSelectorLatestUpdatedAt(factory.newXMLGregorianCalendar(gc));
                GregorianCalendar since = new GregorianCalendar();
                since.setTime(oldest);
                leadSelector.setOldestUpdatedAt(factory.newXMLGregorianCalendar(since));
                leadSelector.setLatestUpdatedAt(until);
                request.setLeadSelector(leadSelector);
            } catch (ParseException | DatatypeConfigurationException e) {
                LOG.error("Error for LastUpdateAtSelector : {}.", e.getMessage());
                throw new ComponentException(e);
            }
        } else
        //
        // Request Using StaticList Selector
        //
        if (parameters.leadSelectorSOAP.getValue().equals(StaticListSelector)) {
            LOG.info("StaticListSelector - List type : {} with value : {}.", parameters.listParam.getValue(),
                    parameters.listParamValue.getValue());

            StaticListSelector staticListSelector = new StaticListSelector();
            ObjectFactory objectFactory = new ObjectFactory();
            if (parameters.listParam.getValue().equals(STATIC_LIST_NAME)) {
                JAXBElement<String> listName = objectFactory
                        .createStaticListSelectorStaticListName(parameters.listParamValue.getValue());
                staticListSelector.setStaticListName(listName);

            } else {
                // you can find listId by examining the URL : https://app-abq.marketo.com/#ST29912B2
                // #ST29912B2 :
                // #ST -> Static list identifier
                // 29912 -> our list FIELD_ID !
                // B2 -> tab in the UI
                JAXBElement<Integer> listId = objectFactory
                        .createStaticListSelectorStaticListId(parseInt(parameters.listParamValue.getValue())); //
                staticListSelector.setStaticListId(listId);
            }
            request.setLeadSelector(staticListSelector);
        } else {
            // Duh !
            LOG.error("Unknown LeadSelector : {}.", parameters.leadSelectorSOAP.getValue());
            throw new ComponentException(
                    new Exception("Incorrect parameter value for LeadSelector : " + parameters.leadSelectorSOAP.getValue()));
        }
        // attributes
        // curiously we have to put some basic fields like Email in attributes if we have them feed...
        ArrayOfString attributes = new ArrayOfString();
        for (String s : mappings.keySet()) {
            attributes.getStringItems().add(s);
        }
        attributes.getStringItems().add("Company");
        request.setIncludeAttributes(attributes);
        // batchSize : another curious behavior... Don't seem to work properly with leadKeySelector...
        // nevertheless, the server automatically adjust batch size according request.
        JAXBElement<Integer> batchSize = new ObjectFactory().createParamsGetMultipleLeadsBatchSize(bSize);
        request.setBatchSize(batchSize);
        // stream position
        if (offset != null && !offset.isEmpty()) {
            request.setStreamPosition(new ObjectFactory().createParamsGetMultipleLeadsStreamPosition(offset));
        }
        //
        long tPrep = currentTimeMillis();
        //
        // Request execution
        //
        SuccessGetMultipleLeads result = null;
        MarketoRecordResult mkto = new MarketoRecordResult();
        LOG.debug("Sending request to server...");
        try {
            result = port.getMultipleLeads(request, header);
        } catch (Exception e) {
            LOG.error("Lead not found : {}.", e.getMessage());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(new MarketoError(SOAP, e.getMessage())));
            return mkto;
        }

        long tRequest = currentTimeMillis();

        if (result == null || !(result.getResult().getReturnCount() > 0)) {
            LOG.debug("Request returned 0 matching leads.");
            mkto.setSuccess(true);
            mkto.setErrors(Arrays.asList(new MarketoError(SOAP, "No leads found.")));
            mkto.setRecordCount(0);
            mkto.setRemainCount(0);
            return mkto;
        } else {

            String streamPos = result.getResult().getNewStreamPosition();
            int recordCount = result.getResult().getReturnCount();
            int remainCount = result.getResult().getRemainingCount();

            // Process results
            results = convertLeadRecords(result.getResult().getLeadRecordList().getValue().getLeadRecords(), schema, mappings);
            long tConv = currentTimeMillis();

            LOG.info("Results : count = {}; remaing = {}; Stream Position = {}.", recordCount, remainCount, streamPos);
            LOG.info("Timings : Preparation = {}ms; Request = {}ms; Convertion = {}ms.", (tPrep - start), (tRequest - tPrep),
                    (tConv - tRequest));
            return new MarketoRecordResult(true, streamPos, recordCount, remainCount, results);
        }
    }

    @Override
    public MarketoRecordResult getLeadActivity(TMarketoInputProperties parameters, String offset) {
        LOG.debug("MarketoSOAPClient.getLeadActivity with {}", parameters.leadKeyTypeSOAP.getValue());
        long start = currentTimeMillis();
        List<IndexedRecord> results = new ArrayList<>();

        Schema schema = parameters.schemaInput.schema.getValue();
        Map<String, String> mappings = parameters.mappingInput.getNameMappingsForMarketo();
        int bSize = parameters.batchSize.getValue();
        String lkt = parameters.leadKeyTypeSOAP.getValue().toString();
        String lkv = parameters.leadKeyValue.getValue();
        //
        // Create Request
        //
        LOG.info("LeadKeySelector - Key type:  {} with value : {}.", lkt, lkv);
        ParamsGetLeadActivity request = new ParamsGetLeadActivity();

        LeadKey key = new LeadKey();
        key.setKeyType(valueOf(lkt));
        key.setKeyValue(lkv);
        request.setLeadKey(key);
        // attributes
        ArrayOfString attributes = new ArrayOfString();
        for (String s : mappings.keySet()) {
            attributes.getStringItems().add(s);
        }
        // Activity filter
        ActivityTypeFilter filter = new ActivityTypeFilter();

        if (parameters.setIncludeTypes.getValue()) {
            ArrayOfActivityType includes = new ArrayOfActivityType();
            for (String a : parameters.includeTypes.type.getValue()) {
                includes.getActivityTypes().add(fromValue(a));
            }
            filter.setIncludeTypes(includes);
        }
        if (parameters.setExcludeTypes.getValue()) {
            ArrayOfActivityType excludes = new ArrayOfActivityType();
            for (String a : parameters.excludeTypes.type.getValue()) {
                excludes.getActivityTypes().add(fromValue(a));
            }
            filter.setExcludeTypes(excludes);
        }

        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<ActivityTypeFilter> typeFilter = objectFactory.createParamsGetLeadActivityActivityFilter(filter);
        request.setActivityFilter(typeFilter);
        // batch size
        JAXBElement<Integer> batchSize = objectFactory.createParamsGetMultipleLeadsBatchSize(bSize);
        request.setBatchSize(batchSize);
        // stream position
        if (offset != null && !offset.isEmpty()) {
            StreamPosition sposition = new StreamPosition();
            sposition.setOffset(objectFactory.createStreamPositionOffset(offset));
            JAXBElement<StreamPosition> position = objectFactory.createParamsGetLeadActivityStartPosition(sposition);
            request.setStartPosition(position);
        }
        //
        long tPrep = currentTimeMillis();
        //
        // Request execution
        //
        SuccessGetLeadActivity result = null;
        LOG.debug("Sending request to server...");
        try {
            result = port.getLeadActivity(request, header);
        } catch (Exception e) {
            LOG.error("Lead not found : {}.", e.getMessage());
        }

        long tRequest = currentTimeMillis();

        if (result == null || !(result.getLeadActivityList().getReturnCount() > 0)) {
            LOG.debug("Request returned 0 matching leads.");
            return new MarketoRecordResult();
        }

        // todo correct stream position
        String streamPos = result.getLeadActivityList().getNewStartPosition().getOffset().getValue();
        int recordCount = result.getLeadActivityList().getReturnCount();
        int remainCount = result.getLeadActivityList().getRemainingCount();

        // Process results
        results = convertLeadActivityRecords(result.getLeadActivityList().getActivityRecordList().getValue().getActivityRecords(),
                schema, mappings);
        long tConv = currentTimeMillis();

        LOG.info("Results : count = {}; remaing = {}; Stream Position = {}.", recordCount, remainCount, streamPos);
        LOG.info("Timings : Preparation = {}ms; Request = {}ms; Convertion = {}ms.", (tPrep - start), (tRequest - tPrep),
                (tConv - tRequest));

        return new MarketoRecordResult(true, streamPos, recordCount, remainCount, results);
    }

    @Override
    public MarketoRecordResult getLeadChanges(TMarketoInputProperties parameters, String offset) {
        LOG.debug("MarketoSOAPClient.getLeadChangesJSON - since {} to  {}.", parameters.oldestCreateDate.getValue(),
                parameters.latestCreateDate.getValue());
        long start = currentTimeMillis();
        List<IndexedRecord> results = new ArrayList<>();
        Schema schema = parameters.schemaInput.schema.getValue();
        Map<String, String> mappings = parameters.mappingInput.getNameMappingsForMarketo();
        int bSize = parameters.batchSize.getValue() > 100 ? 100 : parameters.batchSize.getValue();
        String sOldest = parameters.oldestCreateDate.getValue();
        String sLatest = parameters.latestCreateDate.getValue();
        //
        // Create Request
        //
        LOG.info("LeadChanges - from {} to {}.", sOldest, sLatest);
        ParamsGetLeadChanges request = new ParamsGetLeadChanges();
        LastUpdateAtSelector leadSelector = new LastUpdateAtSelector();
        try {
            DateFormat df = new SimpleDateFormat(DATETIME_PATTERN_PARAM);
            Date oldest = df.parse(sOldest);
            Date latest = df.parse(sLatest);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(latest);
            DatatypeFactory factory = newInstance();
            ObjectFactory objectFactory = new ObjectFactory();
            JAXBElement<XMLGregorianCalendar> until = objectFactory
                    .createLastUpdateAtSelectorLatestUpdatedAt(factory.newXMLGregorianCalendar(gc));
            GregorianCalendar since = new GregorianCalendar();
            since.setTime(oldest);
            leadSelector.setOldestUpdatedAt(factory.newXMLGregorianCalendar(since));
            leadSelector.setLatestUpdatedAt(until);
            request.setLeadSelector(leadSelector);

            JAXBElement<XMLGregorianCalendar> oldestCreateAtValue = objectFactory
                    .createStreamPositionOldestCreatedAt(factory.newXMLGregorianCalendar(since));

            StreamPosition sp = new StreamPosition();
            sp.setOldestCreatedAt(oldestCreateAtValue);
            if (offset != null && !offset.isEmpty()) {
                sp.setOffset(objectFactory.createStreamPositionOffset(offset));
            }
            request.setStartPosition(sp);

        } catch (ParseException | DatatypeConfigurationException e) {
            LOG.error("Error for LastUpdateAtSelector : {}.", e.getMessage());
            throw new ComponentException(e);
        }

        // attributes
        ArrayOfString attributes = new ArrayOfString();
        for (String s : mappings.keySet()) {
            attributes.getStringItems().add(s);
        }
        // Activity filter
        ActivityTypeFilter filter = new ActivityTypeFilter();

        if (parameters.setIncludeTypes.getValue()) {
            ArrayOfActivityType includes = new ArrayOfActivityType();
            for (String a : parameters.includeTypes.type.getValue()) {
                includes.getActivityTypes().add(fromValue(a));
            }
            filter.setIncludeTypes(includes);
        }
        if (parameters.setExcludeTypes.getValue()) {
            ArrayOfActivityType excludes = new ArrayOfActivityType();
            for (String a : parameters.excludeTypes.type.getValue()) {
                excludes.getActivityTypes().add(fromValue(a));
            }
            filter.setExcludeTypes(excludes);
        }

        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<ActivityTypeFilter> typeFilter = objectFactory.createParamsGetLeadActivityActivityFilter(filter);
        request.setActivityFilter(typeFilter);
        // batch size
        JAXBElement<Integer> batchSize = objectFactory.createParamsGetMultipleLeadsBatchSize(bSize);
        request.setBatchSize(batchSize);
        //
        long tPrep = currentTimeMillis();
        //
        // Request execution
        //
        SuccessGetLeadChanges result = null;
        LOG.debug("Sending request to server...");
        try {
            result = port.getLeadChanges(request, header);
        } catch (Exception e) {
            LOG.error("Lead not found : {}.", e.getMessage());
        }

        long tRequest = currentTimeMillis();

        if (result == null || !(result.getResult().getReturnCount() > 0)) {
            LOG.debug("Request returned 0 matching leads.");
            return new MarketoRecordResult(false, null, 0, 0, results);
        }

        // todo correct stream position
        String streamPos = result.getResult().getNewStartPosition().getOffset().getValue();
        int recordCount = result.getResult().getReturnCount();
        int remainCount = result.getResult().getRemainingCount();

        // Process results
        results = convertLeadChangeRecords(result.getResult().getLeadChangeRecordList().getValue().getLeadChangeRecords(), schema,
                mappings);
        long tConv = currentTimeMillis();

        LOG.info("Results : count = {}; remaing = {}; Stream Position = {}.", recordCount, remainCount, streamPos);
        LOG.info("Timings : Preparation = {}ms; Request = {}ms; Convertion = {}ms.", (tPrep - start), (tRequest - tPrep),
                (tConv - tRequest));

        return new MarketoRecordResult(true, streamPos, recordCount, remainCount, results);
    }

    public MarketoSyncResult listOperation(ListOperationType operationType, ListOperationParameters parameters) {
        LOG.debug("listOperation : {}", parameters);
        ParamsListOperation paramsListOperation = new ParamsListOperation();
        paramsListOperation.setListOperation(operationType);
        paramsListOperation.setStrict(objectFactory.createParamsListOperationStrict(parameters.getStrict()));
        ListKey listKey = new ListKey();
        listKey.setKeyValue(parameters.getListKeyValue());
        listKey.setKeyType(ListKeyType.valueOf(parameters.getListKeyType()));
        paramsListOperation.setListKey(listKey);
        ArrayOfLeadKey leadKeys = new ArrayOfLeadKey();
        for (String lkv : parameters.getLeadKeyValues()) {
            LeadKey lk = new LeadKey();
            lk.setKeyType(LeadKeyRef.valueOf(parameters.getLeadKeyType()));
            lk.setKeyValue(lkv);
            leadKeys.getLeadKeies().add(lk);
        }
        paramsListOperation.setListMemberList(leadKeys);

        MarketoSyncResult mkto = new MarketoSyncResult();
        mkto.setRequestId(SOAP + "::" + operationType.name());
        try {
            SuccessListOperation result = port.listOperation(paramsListOperation, header);
            if (LOG.isDebugEnabled()) {
                try {
                    JAXBContext context = JAXBContext.newInstance(SuccessListOperation.class);
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    m.marshal(result, System.out);
                } catch (JAXBException e) {
                    LOG.error(e.getMessage());
                }
            }
            mkto.setSuccess(true);
            if (!result.getResult().getStatusList().isNil()) {
                mkto.setRecordCount(result.getResult().getStatusList().getValue().getLeadStatuses().size());
                List<LeadStatus> statuses = result.getResult().getStatusList().getValue().getLeadStatuses();
                List<SyncStatus> resultStatus = new ArrayList<>();
                for (LeadStatus status : statuses) {
                    SyncStatus sts = new SyncStatus(Integer.parseInt(status.getLeadKey().getKeyValue()),
                            String.valueOf(status.isStatus()));
                    if (!status.isStatus() && !ISMEMBEROFLIST.equals(operationType)) {
                        Map<String, String> reason = new HashMap<>();
                        reason.put("code", "20103");
                        reason.put("message", "Lead Not Found");
                        sts.setReasons(Collections.singletonList(reason));
                    }
                    resultStatus.add(sts);
                }
                mkto.setRecords(resultStatus);
            } else {
                LOG.debug("No detail about successed operation, building one...");
                String success = String.valueOf(result.getResult().isSuccess());
                mkto.setRecordCount(parameters.getLeadKeyValue().size());
                for (String leadk : parameters.getLeadKeyValue()) {
                    SyncStatus status = new SyncStatus(Integer.parseInt(leadk), success);
                    mkto.getRecords().add(status);
                }
            }
        } catch (Exception e) {
            LOG.error("[{}] error: {}", operationType.name(), e.getMessage());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(new MarketoError(SOAP, e.toString())));
        }
        return mkto;
    }

    @Override
    public MarketoSyncResult addToList(ListOperationParameters parameters) {
        return listOperation(ListOperationType.ADDTOLIST, parameters);
    }

    @Override
    public MarketoSyncResult isMemberOfList(ListOperationParameters parameters) {
        return listOperation(ISMEMBEROFLIST, parameters);
    }

    @Override
    public MarketoSyncResult removeFromList(ListOperationParameters parameters) {
        return listOperation(ListOperationType.REMOVEFROMLIST, parameters);
    }
    /*
     * 
     * SyncLeads operations
     * 
     */

    public LeadRecord convertToLeadRecord(IndexedRecord record, Map<String, String> mappings) throws MarketoException {
        // first, check if a mandatory field is in the schema
        Boolean ok = Boolean.FALSE;
        for (Entry<String, String> e : mappings.entrySet()) {
            ok |= (e.getKey().equals(FIELD_ID) || e.getKey().equals(FIELD_EMAIL) || e.getKey().equals(FIELD_FOREIGN_SYS_PERSON_ID)
                    || e.getValue().equals(FIELD_ID) || e.getValue().equals(FIELD_EMAIL)
                    || e.getValue().equals(FIELD_FOREIGN_SYS_PERSON_ID))
                    && record.get(record.getSchema().getField(e.getKey()).pos()) != null;
        }
        if (!ok) {
            MarketoException err = new MarketoException("SOAP", "syncLead error: Missing mandatory field for operation.");
            LOG.error(err.toString());
            throw err;
        }
        //
        LeadRecord lead = new LeadRecord();
        ArrayOfAttribute aoa = new ArrayOfAttribute();
        for (Field f : record.getSchema().getFields()) {
            // find matching marketo column name
            String col = mappings.get(f.name());
            if (col.equals(FIELD_ID)) {
                final Integer id = (Integer) record.get(f.pos());
                if (id != null) {
                    lead.setId(objectFactory.createLeadRecordId(id));
                }
            } else if (col.equals(FIELD_EMAIL)) {
                final String email = (String) record.get(f.pos());
                if (email != null) {
                    lead.setEmail(objectFactory.createLeadRecordEmail(email));
                }
            } else if (col.equals(FIELD_FOREIGN_SYS_PERSON_ID)) {
                final String fspid = (String) record.get(f.pos());
                if (fspid != null) {
                    lead.setForeignSysPersonId(objectFactory.createLeadRecordForeignSysPersonId(fspid));
                }
            } else if (col.equals(FIELD_FOREIGN_SYS_TYPE)) {
                final String fst = (String) record.get(f.pos());
                if (fst != null) {
                    lead.setForeignSysType(objectFactory.createLeadRecordForeignSysType(ForeignSysType.valueOf(fst)));
                }
            } else {
                // skip status & error fields
                if (FIELD_STATUS.equals(col) || FIELD_ERROR_MSG.equals(col)) {
                    continue;
                }
                Attribute attr = new Attribute();
                attr.setAttrName(col);
                attr.setAttrValue((String) record.get(f.pos()));
                aoa.getAttributes().add(attr);
            }
        }
        QName qname = new QName("http://www.marketo.com/mktows/", "leadAttributeList");
        JAXBElement<ArrayOfAttribute> attrList = new JAXBElement(qname, ArrayOfAttribute.class, aoa);
        lead.setLeadAttributeList(attrList);

        return lead;
    }

    /**
     * Request<br/>
     * 
     * Field Name <br/>
     * <code>leadRecord->Id</code> Required – Only when Email or foreignSysPersonId is not present The Marketo Id of the
     * lead record<br/>
     * <code>leadRecord->Email</code> Required – Only when Id or foreignSysPersonId is not present The email address
     * associated with the lead record<br/>
     * <code>leadRecord->foreignSysPersonId</code> Required – Only when Id or Email is not present The foreign system id
     * associated with the lead record<br/>
     * <code>leadRecord->foreignSysType</code> Optional – Only required when foreignSysPersonId is present The type of
     * foreign system. Possible values: CUSTOM, SFDC, NETSUITE<br/>
     * <code>leadRecord->leadAttributeList->attribute->attrName</code> Required The name of the lead attribute you want
     * to update the value of.<br/>
     * <code>leadRecord->leadAttributeList->attribute->attrValue</code> Required The value you want to set to the lead
     * attribute specificed in attrName. returnLead Required When true will return the complete updated lead record upon
     * update.<br/>
     * <code>marketoCookie</code> Optional The Munchkin javascript cookie<br/>
     */
    @Override
    public MarketoSyncResult syncLead(TMarketoOutputProperties parameters, IndexedRecord lead) {
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            ParamsSyncLead request = new ParamsSyncLead();
            // fixme managed or not ?
            request.setReturnLead(false);
            //
            request.setLeadRecord(convertToLeadRecord(lead, parameters.mappingInput.getNameMappingsForMarketo()));
            MktowsContextHeader headerContext = new MktowsContextHeader();
            headerContext.setTargetWorkspace("default");
            SuccessSyncLead result = port.syncLead(request, header, headerContext);
            //
            if (LOG.isDebugEnabled()) {
                try {
                    JAXBContext context = JAXBContext.newInstance(SuccessSyncLead.class);
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    m.marshal(result, System.out);
                } catch (JAXBException e) {
                    LOG.error(e.getMessage());
                }
            }
            //
            com.marketo.mktows.SyncStatus status = result.getResult().getSyncStatus();
            mkto.setSuccess(status.getError().isNil());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(1);
                SyncStatus resultStatus = new SyncStatus(status.getLeadId(), status.getStatus().value());
                mkto.setRecords(Arrays.asList(resultStatus));
            } else {
                mkto.setErrors(Arrays.asList(new MarketoError(SOAP, status.getError().getValue())));
            }
        } catch (Exception e) {
            LOG.error(e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(new MarketoError(SOAP, e.getMessage())));
        }
        return mkto;
    }

    @Override
    public MarketoSyncResult syncMultipleLeads(TMarketoOutputProperties parameters, List<IndexedRecord> leads) {
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            ParamsSyncMultipleLeads request = new ParamsSyncMultipleLeads();
            ArrayOfLeadRecord leadRecords = new ArrayOfLeadRecord();
            for (IndexedRecord r : leads) {
                leadRecords.getLeadRecords().add(convertToLeadRecord(r, parameters.mappingInput.getNameMappingsForMarketo()));
            }
            JAXBElement<Boolean> dedup = objectFactory
                    .createParamsSyncMultipleLeadsDedupEnabled(parameters.deDupeEnabled.getValue());
            request.setDedupEnabled(dedup);
            request.setLeadRecordList(leadRecords);
            SuccessSyncMultipleLeads result = port.syncMultipleLeads(request, header);
            //
            if (LOG.isDebugEnabled()) {
                try {
                    JAXBContext context = JAXBContext.newInstance(SuccessSyncLead.class);
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    m.marshal(result, System.out);
                } catch (JAXBException e) {
                    LOG.error(e.getMessage());
                }
            }
            //
            List<SyncStatus> records = new ArrayList<>();
            for (com.marketo.mktows.SyncStatus status : result.getResult().getSyncStatusList().getSyncStatuses()) {
                SyncStatus s = new SyncStatus(status.getLeadId(), status.getStatus().value());
                s.setErrorMessage(status.getError().getValue());
                records.add(s);
            }
            mkto.setSuccess(result.getResult().getSyncStatusList() != null);
            mkto.setRecords(records);
        } catch (Exception e) {
            LOG.error(e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(new MarketoError(SOAP, e.getMessage())));
        }
        return mkto;
    }
}
