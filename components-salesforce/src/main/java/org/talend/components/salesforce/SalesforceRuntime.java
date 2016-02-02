// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce;

import java.io.BufferedWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentDynamicHolder;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.api.runtime.ComponentRuntimeContainer;
import org.talend.components.api.service.ComponentService;
import org.talend.components.salesforce.connection.oauth.SalesforceOAuthConnection;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaFactory;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.SessionHeader_element;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;
import com.sforce.ws.bind.XmlObject;

public class SalesforceRuntime extends ComponentRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceRuntime.class);

    protected static final String API_VERSION = "34.0";

    protected ComponentService componentService;

    protected ComponentRuntimeContainer container;

    protected ComponentProperties properties;

    protected PartnerConnection connection;

    protected BulkConnection bulkConnection;

    protected boolean exceptionForErrors;

    protected BufferedWriter logWriter;

    protected int commitLevel;

    protected List<String> deleteItems;

    protected List<SObject> insertItems;

    protected List<SObject> upsertItems;

    protected List<SObject> updateItems;

    protected QueryResult inputResult;

    protected SObject[] inputRecords;

    protected int inputRecordsIndex;

    protected String upsertKeyColumn;

    protected Map<String, SchemaElement> fieldMap;

    protected List<SchemaElement> fieldList;

    /*
     * Used on input only, this is read from the module schema, it contains all of the fields from the salesforce
     * definition of the module that are not already in the field list.
     */
    protected List<SchemaElement> dynamicFieldList;

    protected Map<String, SchemaElement> dynamicFieldMap;

    /*
     * The actual fields we read on input which is a combination of the fields specified in the schema and the dynamic
     * fields.
     */
    protected List<SchemaElement> inputFieldsToUse;

    /*
     * The dynamic column that is specified on the input schema.
     */
    protected SchemaElement dynamicField;

    public SalesforceRuntime() {
        commitLevel = 1;
        int arraySize = commitLevel * 2;
        deleteItems = new ArrayList<String>(arraySize);
        insertItems = new ArrayList<SObject>(arraySize);
        updateItems = new ArrayList<SObject>(arraySize);
        upsertItems = new ArrayList<SObject>(arraySize);
        upsertKeyColumn = "";
    }

    public SalesforceRuntime(ComponentRuntimeContainer container) {
        this();
        setContainer(container);
    }

    public SalesforceRuntime(ComponentRuntimeContainer context, int commitLevel, boolean exceptionForErrors, String errorLogFile)
            throws Exception {
        this(context);

        if (commitLevel <= 0) {
            commitLevel = 1;
        } else if (commitLevel > 200) {
            commitLevel = 200;
        }

        this.commitLevel = commitLevel;
        this.exceptionForErrors = exceptionForErrors;
        if (errorLogFile != null && errorLogFile.trim().length() > 0) {
            logWriter = new java.io.BufferedWriter(new java.io.FileWriter(errorLogFile));
        }
    }

    @Override
    public void setContainer(ComponentRuntimeContainer container) {
        this.container = container;
    }

    public void setComponentService(ComponentService service) {
        componentService = service;
    }

    protected void connectBulk(SalesforceConnectionProperties properties, ConnectorConfig config) throws AsyncApiException {
        /*
         * When PartnerConnection is instantiated, a login is implicitly executed and, if successful, a valid session is
         * stored in the ConnectorConfig instance. Use this key to initialize a BulkConnection:
         */
        ConnectorConfig bulkConfig = new ConnectorConfig();
        bulkConfig.setSessionId(config.getSessionId());
        /*
         * The endpoint for the Bulk API service is the same as for the normal SOAP uri until the /Soap/ part. From here
         * it's '/async/versionNumber'
         */
        String soapEndpoint = config.getServiceEndpoint();
        // FIXME - fix hardcoded version
        String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + API_VERSION;
        bulkConfig.setRestEndpoint(restEndpoint);
        // This should only be false when doing debugging.
        bulkConfig.setCompression(true);
        bulkConfig.setTraceMessage(false);
        bulkConnection = new BulkConnection(bulkConfig);
        if (container != null) {
            String currentComponent = container.getCurrentComponentName();
            if (currentComponent != null && currentComponent.startsWith(TSalesforceConnectionDefinition.COMPONENT_NAME)) {
                container.getGlobalMap().put(currentComponent, bulkConnection);
            }
        }
    }

    protected void doConnection(SalesforceConnectionProperties properties, ConnectorConfig config)
            throws AsyncApiException, ConnectionException {
        if (SalesforceConnectionProperties.LOGIN_OAUTH.equals(properties.loginType.getValue())) {
            SalesforceOAuthConnection oauthConnection = new SalesforceOAuthConnection(properties.oauth,
                    SalesforceConnectionProperties.OAUTH_URL, API_VERSION);
            oauthConnection.login(config);
        } else {
            config.setAuthEndpoint(SalesforceConnectionProperties.URL);
        }
        connection = new PartnerConnection(config);
        if (container != null) {
            String currentComponent = container.getCurrentComponentName();
            if (currentComponent != null && currentComponent.startsWith(TSalesforceConnectionDefinition.COMPONENT_NAME)) {
                container.getGlobalMap().put(currentComponent, connection);
            }
        }
        if (properties.bulkConnection.getBooleanValue()) {
            connectBulk(properties, config);
        }
    }

    @Override
    public ValidationResult connectWithResult(ComponentProperties p) {
        SalesforceConnectionProperties properties = (SalesforceConnectionProperties) p;
        ValidationResult vr = new ValidationResult();
        try {
            connect(properties);
        } catch (LoginFault ex) {
            vr.setMessage(ex.getExceptionMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        } catch (Exception ex) {
            // FIXME - do a better job here
            vr.setMessage(ex.getMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        return vr;
    }

    @Override
    public void connect(ComponentProperties p) throws ConnectionException, AsyncApiException {
        SalesforceConnectionProperties properties = (SalesforceConnectionProperties) p;
        String refedComponentId = properties.referencedComponentId.getStringValue();
        if (refedComponentId != null && container != null) {
            if (!refedComponentId.equals(container.getCurrentComponentName())) {
                connection = (PartnerConnection) container.getGlobalMap().get(refedComponentId);
                if (connection == null) {
                    throw new ConnectionException(
                            "Can't find the shared connection instance with refedComponentId: " + refedComponentId);
                }
                return;
            }
        }

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(StringUtils.strip(properties.userPassword.userId.getStringValue(), "\""));
        config.setPassword(StringUtils.strip(properties.userPassword.password.getStringValue(), "\"")
                + StringUtils.strip(properties.userPassword.securityKey.getStringValue(), "\""));

        // Notes on how to test this
        // http://thysmichels.com/2014/02/15/salesforce-wsc-partner-connection-session-renew-when-session-timeout/

        final SalesforceConnectionProperties finalProps = properties;
        config.setSessionRenewer(new SessionRenewer() {

            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                SessionRenewalHeader header = new SessionRenewalHeader();
                try {
                    // FIXME - session id need to be null for trigger the login?
                    // connectorConfig.setSessionId(null);
                    doConnection(finalProps, connectorConfig);
                } catch (AsyncApiException e) {
                    // FIXME
                    e.printStackTrace();
                }

                SessionHeader_element h = connection.getSessionHeader();
                // FIXME - one or the other, I have seen both
                // header.name = new QName("urn:partner.soap.sforce.com", "X-SFDC-Session");
                header.name = new QName("urn:partner.soap.sforce.com", "SessionHeader");
                header.headerElement = h.getSessionId();
                return header;
            }
        });

        if (properties.timeout.getIntValue() > 0) {
            config.setConnectionTimeout(properties.timeout.getIntValue());
        }
        config.setCompression(properties.needCompression.getBooleanValue());
        if (false) {
            config.setTraceMessage(true);
        }

        doConnection(properties, config);

        LOG.debug("Connection: " + connection);
        LOG.debug("Bulk Connection: " + bulkConnection);

    }

    @Override
    public List<NamedThing> getSchemaNames() throws ConnectionException {
        List<NamedThing> returnList = new ArrayList<>();
        DescribeGlobalResult result = connection.describeGlobal();
        DescribeGlobalSObjectResult[] objects = result.getSobjects();
        for (DescribeGlobalSObjectResult obj : objects) {
            LOG.debug("module label: " + obj.getLabel() + " name: " + obj.getName());
            returnList.add(new SimpleNamedThing(obj.getName(), obj.getLabel()));
        }
        return returnList;
    }

    public void setupSchemaElement(Field field, SchemaElement element) {
        String type = field.getType().toString();
        if (type.equals("boolean")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.BOOLEAN);
        } else if (type.equals("int")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.INT);
        } else if (type.equals("date")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DATE);
            element.setPattern("\"yyyy-MM-dd\""); //$NON-NLS-1$
        } else if (type.equals("datetime")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DATETIME);
            element.setPattern("\"yyyy-MM-dd\'T\'HH:mm:ss\'.000Z\'\""); //$NON-NLS-1$
        } else if (type.equals("double")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DOUBLE);
        } else if (type.equals("currency")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DECIMAL);
        }
        element.setNullable(field.getNillable());

        if (element.getType() == SchemaElement.Type.STRING) {
            element.setSize(field.getLength());
            element.setPrecision(field.getPrecision());
        } else {
            element.setSize(field.getPrecision());
            element.setPrecision(field.getScale());
        }
        element.setDefaultValue(field.getDefaultValueFormula());
    }

    @Override
    public Schema getSchema(String module) throws ConnectionException {
        Schema schema = SchemaFactory.newSchema();
        SchemaElement root = SchemaFactory.newSchemaElement("Root");
        schema.setRoot(root);

        DescribeSObjectResult[] describeSObjectResults = connection.describeSObjects(new String[] { module });
        Field fields[] = describeSObjectResults[0].getFields();
        for (Field field : fields) {
            SchemaElement child = PropertyFactory.newProperty(field.getName());
            setupSchemaElement(field, child);
            root.addChild(child);
        }
        return schema;
    }

    protected void commonBegin(ComponentProperties props) throws ConnectionException, AsyncApiException {
        properties = props;
        if (!(props instanceof SalesforceConnectionModuleProperties)) {
            return;
        }

        SalesforceConnectionModuleProperties sprops = (SalesforceConnectionModuleProperties) props;
        connect(sprops.connection);

        Schema schema = sprops.getSchema();
        fieldMap = schema.getRoot().getChildMap();
        fieldList = schema.getRoot().getChildren();

        for (SchemaElement se : fieldList) {
            if (se.getType() == SchemaElement.Type.DYNAMIC) {
                dynamicField = se;
                break;
            }
        }
    }

    @Override
    public void inputBegin(ComponentProperties props) throws Exception {
        TSalesforceInputProperties sprops = (TSalesforceInputProperties) props;
        commonBegin(props);

        connection.setQueryOptions(sprops.batchSize.getIntValue());

        /*
         * Dynamic columns are requested, find them from Salesforce and only look at the ones that are not explicitly
         * specified in the schema.
         */
        if (dynamicField != null) {
            dynamicFieldMap = new HashMap<>();
            List<SchemaElement> filteredDynamicFields = new ArrayList<>();
            Schema dynSchema = getSchema(sprops.module.moduleName.getStringValue());

            for (SchemaElement se : dynSchema.getRoot().getChildren()) {
                if (fieldMap.containsKey(se.getName())) {
                    continue;
                }
                filteredDynamicFields.add(se);
                dynamicFieldMap.put(se.getName(), se);
            }
            dynamicFieldList = filteredDynamicFields;
        }

        inputFieldsToUse = new ArrayList<>();
        for (SchemaElement s : fieldList) {
            if (s.getType() == SchemaElement.Type.DYNAMIC) {
                continue;
            }
            inputFieldsToUse.add(s);
        }
        if (dynamicFieldList != null) {
            inputFieldsToUse.addAll(dynamicFieldList);
        }

        String queryText;
        if (sprops.manualQuery.getBooleanValue()) {
            queryText = sprops.query.getStringValue();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            int count = 0;
            for (SchemaElement se : inputFieldsToUse) {
                if (count++ > 0) {
                    sb.append(", ");
                }
                sb.append(se.getName());
            }
            sb.append(" from ");
            sb.append(sprops.module.moduleName.getStringValue());
            queryText = sb.toString();
        }

        inputResult = connection.query(queryText);
        inputRecords = inputResult.getRecords();
        inputRecordsIndex = 0;
    }

    @Override
    public Map<String, Object> inputRow() throws Exception {
        if (inputRecordsIndex >= inputRecords.length) {
            if (inputResult.isDone()) {
                return null;
            }
            inputResult = connection.queryMore(inputResult.getQueryLocator());
            inputRecordsIndex = 0;
        }

        ComponentDynamicHolder dynamicHolder = null;
        if (dynamicField != null) {
            dynamicHolder = container.createDynamicHolder();
            dynamicHolder.setSchemaElements(dynamicFieldList);
        }
        Iterator<XmlObject> it = inputRecords[inputRecordsIndex++].getChildren();
        Map<String, Object> columns = new HashMap<>();
        while (it.hasNext()) {
            XmlObject obj = it.next();
            String localName = obj.getName().getLocalPart();
            if (dynamicFieldMap != null && dynamicFieldMap.get(localName) != null) {
                dynamicHolder.addFieldValue(localName, obj.getValue());
            } else {
                columns.put(localName, obj.getValue());
            }
        }
        if (dynamicHolder != null) {
            columns.put(dynamicField.getName(), dynamicHolder);
        }
        return columns;
    }

    @Override
    public void inputEnd() throws Exception {
        logout();
    }

    @Override
    public void outputBegin(ComponentProperties props) throws Exception {
        commonBegin(props);

        TSalesforceOutputProperties sprops = (TSalesforceOutputProperties) props;
        upsertKeyColumn = sprops.upsertKeyColumn.getStringValue();
    }

    @Override
    public void outputMain(Map<String, Object> row) throws Exception {
        TSalesforceOutputProperties sprops = (TSalesforceOutputProperties) properties;
        if (!TSalesforceOutputProperties.ACTION_DELETE.equals(sprops.outputAction.getValue())) {
            SObject so = new SObject();
            so.setType(sprops.module.moduleName.getStringValue());

            for (String key : row.keySet()) {
                Object value = row.get(key);
                if (value != null) {
                    SchemaElement se = fieldMap.get(key);
                    if (se != null && se.getType() != SchemaElement.Type.DYNAMIC) {
                        addSObjectField(so, se, value);
                    }
                }
            }

            if (dynamicField != null) {
                ComponentDynamicHolder dynamic = (ComponentDynamicHolder) row.get(dynamicField.getName());
                List<SchemaElement> dynamicSes = dynamic.getSchemaElements();
                for (SchemaElement dynamicSe : dynamicSes) {
                    Object value = dynamic.getFieldValue(dynamicSe.getName());
                    addSObjectField(so, dynamicSe, value);
                }
            }

            switch (TSalesforceOutputProperties.OutputAction.valueOf(sprops.outputAction.getStringValue())) {
            case INSERT:
                insert(so);
                break;
            case UPDATE:
                update(so);
                break;
            case UPSERT:
                upsert(so);
                break;
            case DELETE:
                // See below
                throw new RuntimeException("Impossible");
            }
        } else { // DELETE
            String id = getIdValue(row);
            if (id != null) {
                delete(id);
            }
        }
    }

    @Override
    public void outputEnd() throws Exception {
        logout();
    }

    protected String getIdValue(Map<String, Object> row) {
        String ID = "Id";
        if (row.get(ID) != null) {
            SchemaElement se = fieldMap.get(ID);
            if (se.getType() != SchemaElement.Type.DYNAMIC) {
                return (String) row.get(ID);
            }
        }
        // FIXME - need better exception
        if (dynamicField == null) {
            throw new RuntimeException("Expected dynamic column to be available");
        }

        ComponentDynamicHolder dynamic = (ComponentDynamicHolder) row.get(dynamicField.getName());
        List<SchemaElement> dynamicSes = dynamic.getSchemaElements();
        for (SchemaElement dynamicSe : dynamicSes) {
            if (dynamicSe.getName().equals(ID)) {
                return (String) dynamic.getFieldValue(ID);
            }
        }

        // FIXME - need better exception
        throw new RuntimeException(ID + " not found in dynamic columns");
    }

    protected void addSObjectField(SObject sObject, SchemaElement se, Object value) {
        Object valueToAdd;
        switch (se.getType()) {
        case BYTE_ARRAY:
            valueToAdd = Charset.defaultCharset().decode(ByteBuffer.wrap((byte[]) value)).toString();
            break;
        case DATE:
        case DATETIME:
            valueToAdd = container.formatDate((Date) value, se.getPattern());
            break;
        default:
            valueToAdd = value;
            break;
        }
        sObject.setField(se.getName(), valueToAdd);
    }

    protected void logout() throws Exception {
        try {
            // Finish anything uncommitted
            doInsert();
            doDelete();
            doUpdate();
            doUpsert();
        } finally {
            if (logWriter != null) {
                logWriter.close();
            }
        }
    }

    // FIXME - public for tests
    public DeleteResult[] delete(String id) throws Exception {
        if (id == null) {
            return null;
        }
        deleteItems.add(id);
        return doDelete();
    }

    protected DeleteResult[] doDelete() throws Exception {
        if (deleteItems.size() >= commitLevel) {
            String[] delIDs = deleteItems.toArray(new String[deleteItems.size()]);
            String[] changedItemKeys = new String[delIDs.length];
            for (int ix = 0; ix < delIDs.length; ++ix) {
                changedItemKeys[ix] = delIDs[ix];
            }
            DeleteResult[] dr = connection.delete(delIDs);
            deleteItems.clear();

            if (dr != null && dr.length != 0) {
                int batch_idx = -1;
                for (DeleteResult result : dr) {
                    handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                }
            }

            return dr;
        }
        return null;
    }

    protected SaveResult[] insert(SObject sObject) throws Exception {
        insertItems.add(sObject);
        return doInsert();
    }

    protected SaveResult[] doInsert() throws Exception {
        if (insertItems.size() >= commitLevel) {
            SObject[] accs = insertItems.toArray(new SObject[insertItems.size()]);
            String[] changedItemKeys = new String[accs.length];
            SaveResult[] sr = connection.create(accs);
            insertItems.clear();
            if (sr != null && sr.length != 0) {
                int batch_idx = -1;
                for (SaveResult result : sr) {
                    handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                }
            }
            return sr;
        }
        return null;
    }

    protected SaveResult[] update(SObject sObject) throws Exception {
        updateItems.add(sObject);
        return doUpdate();
    }

    protected SaveResult[] doUpdate() throws Exception {
        if (updateItems.size() >= commitLevel) {
            SObject[] upds = updateItems.toArray(new SObject[updateItems.size()]);
            String[] changedItemKeys = new String[upds.length];
            for (int ix = 0; ix < upds.length; ++ix) {
                changedItemKeys[ix] = upds[ix].getId();
            }
            SaveResult[] saveResults = connection.update(upds);
            updateItems.clear();
            upds = null;

            if (saveResults != null && saveResults.length != 0) {
                int batch_idx = -1;
                for (SaveResult result : saveResults) {
                    handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                }
            }
            return saveResults;
        }
        return null;
    }

    protected UpsertResult[] upsert(SObject sObject) throws Exception {
        upsertItems.add(sObject);
        return doUpsert();
    }

    protected UpsertResult[] doUpsert() throws Exception {
        if (upsertItems.size() >= commitLevel) {
            SObject[] upds = upsertItems.toArray(new SObject[upsertItems.size()]);
            String[] changedItemKeys = new String[upds.length];
            for (int ix = 0; ix < upds.length; ++ix) {
                Object value = upds[ix].getField(upsertKeyColumn);
                if (value == null) {
                    changedItemKeys[ix] = "No value for " + upsertKeyColumn + " ";
                } else {
                    changedItemKeys[ix] = upsertKeyColumn;
                }
            }
            UpsertResult[] upsertResults = connection.upsert(upsertKeyColumn, upds);
            upsertItems.clear();
            upds = null;

            if (upsertResults != null && upsertResults.length != 0) {
                int batch_idx = -1;
                for (UpsertResult result : upsertResults) {
                    handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                }
            }
            return upsertResults;
        }
        return null;

    }

    protected void handleResults(boolean success, Error[] resultErrors, String[] changedItemKeys, int batchIdx) throws Exception {
        StringBuilder errors = new StringBuilder("");
        if (success) {
            // TODO: send back the ID
        } else {
            errors = addLog(resultErrors,
                    batchIdx < changedItemKeys.length ? changedItemKeys[batchIdx] : "Batch index out of bounds");
        }
        if (exceptionForErrors && errors.toString().length() > 0) {
            if (logWriter != null) {
                logWriter.close();
            }
            throw new Exception(errors.toString());
        }
    }

    protected StringBuilder addLog(Error[] resultErrors, String row_key) throws Exception {
        StringBuilder errors = new StringBuilder("");
        if (resultErrors != null) {
            for (Error error : resultErrors) {
                errors.append(error.getMessage()).append("\n");
                if (logWriter != null) {
                    logWriter.append("\tStatus Code: ").append(error.getStatusCode().toString());
                    logWriter.newLine();
                    logWriter.newLine();
                    logWriter.append("\tRowKey/RowNo: " + row_key);
                    if (error.getFields() != null) {
                        logWriter.newLine();
                        logWriter.append("\tFields: ");
                        boolean flag = false;
                        for (String field : error.getFields()) {
                            if (flag) {
                                logWriter.append(", ");
                            } else {
                                flag = true;
                            }
                            logWriter.append(field);
                        }
                    }
                    logWriter.newLine();
                    logWriter.newLine();

                    logWriter.append("\tMessage: ").append(error.getMessage());

                    logWriter.newLine();

                    logWriter.append("\t--------------------------------------------------------------------------------");

                    logWriter.newLine();
                    logWriter.newLine();

                }
            }
        }
        return errors;
    }

    protected void populateResultMessage(Map<String, String> resultMessage, Error[] errors) {
        for (Error error : errors) {
            if (error.getStatusCode() != null) {
                resultMessage.put("StatusCode", error.getStatusCode().toString());
            }
            if (error.getFields() != null) {
                StringBuffer fields = new StringBuffer();
                for (String field : error.getFields()) {
                    fields.append(field);
                    fields.append(",");
                }
                if (fields.length() > 0) {
                    fields.deleteCharAt(fields.length() - 1);
                }
                resultMessage.put("Fields", fields.toString());
            }
            resultMessage.put("Message", error.getMessage());
        }
    }

    // FIXME - not sure what this is used for
    protected Map<String, String> readResult(Object[] results) throws Exception {
        Map<String, String> resultMessage = null;
        if (results instanceof SaveResult[]) {
            for (SaveResult result : (SaveResult[]) results) {
                resultMessage = new HashMap<String, String>();
                if (result.getId() != null) {
                    resultMessage.put("id", result.getId());
                }
                resultMessage.put("success", String.valueOf(result.getSuccess()));
                if (!result.getSuccess()) {
                    populateResultMessage(resultMessage, result.getErrors());
                }
            }
            return resultMessage;
        } else if (results instanceof DeleteResult[]) {
            for (DeleteResult result : (DeleteResult[]) results) {
                resultMessage = new HashMap<String, String>();
                if (result.getId() != null) {
                    resultMessage.put("id", result.getId());
                }
                resultMessage.put("success", String.valueOf(result.getSuccess()));
                if (!result.getSuccess()) {
                    populateResultMessage(resultMessage, result.getErrors());
                }
            }
            return resultMessage;
        } else if (results instanceof UpsertResult[]) {
            for (UpsertResult result : (UpsertResult[]) results) {
                resultMessage = new HashMap<String, String>();
                if (result.getId() != null) {
                    resultMessage.put("id", result.getId());
                }
                resultMessage.put("success", String.valueOf(result.getSuccess()));
                resultMessage.put("created", String.valueOf(result.getCreated()));
                if (!result.getSuccess()) {
                    populateResultMessage(resultMessage, result.getErrors());
                }
            }
            return resultMessage;
        }
        return null;
    }

}
