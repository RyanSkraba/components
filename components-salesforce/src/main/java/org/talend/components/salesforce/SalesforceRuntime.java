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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.namespace.QName;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.api.runtime.ComponentRuntimeContainer;
import org.talend.components.api.schema.ComponentSchema;
import org.talend.components.api.schema.ComponentSchemaElement;
import org.talend.components.api.schema.ComponentSchemaFactory;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

public class SalesforceRuntime extends ComponentRuntime {

    private PartnerConnection                   connection;

    private BulkConnection                      bulkConnection;

    private ComponentRuntimeContainer           container;

    private boolean                             exceptionForErrors;

    private java.io.BufferedWriter              logWriter;

    private int                                 commitLevel;

    private List<String>                        deleteItems;

    private List<SObject>                       insertItems;

    private List<SObject>                       upsertItems;

    private List<SObject>                       updateItems;

    private String                              upsertKeyColumn;

    private Map<String, ComponentSchemaElement> fieldMap;

    private List<ComponentSchemaElement>        fieldList;

    /*
     * Used on input only, this is read from the module schema, it contains all of the fields from the salesforce
     * definition of the module that are not already in the field list.
     */
    private List<ComponentSchemaElement>        dynamicFieldList;

    /*
     * The actual fields we read on input which is a combination of the fields specified in the schema and the dynamic
     * fields.
     */
    private List<ComponentSchemaElement>        inputFieldsToUse;

    /*
     * The dynamic column that is specified on the input schema.
     */
    private ComponentSchemaElement              dynamicField;

    public SalesforceRuntime() {
        commitLevel = 1;
        int arraySize = commitLevel * 2;
        deleteItems = new ArrayList<String>(arraySize);
        insertItems = new ArrayList<SObject>(arraySize);
        updateItems = new ArrayList<SObject>(arraySize);
        upsertItems = new ArrayList<SObject>(arraySize);
        upsertKeyColumn = "";
    }

    public SalesforceRuntime(ComponentRuntimeContainer context) {
        this();
        this.container = context;
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
        String apiVersion = "34.0";
        String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
        bulkConfig.setRestEndpoint(restEndpoint);
        // This should only be false when doing debugging.
        bulkConfig.setCompression(true);
        bulkConfig.setTraceMessage(false);
        bulkConnection = new BulkConnection(bulkConfig);
    }

    protected void doConnection(SalesforceConnectionProperties properties, ConnectorConfig config) throws AsyncApiException,
            ConnectionException {
        connection = new PartnerConnection(config);
        if (properties.bulkConnection.isValueTrue()) {
            connectBulk(properties, config);
        }
    }

    public void connect(final SalesforceConnectionProperties properties) throws Exception {

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(properties.userPassword.userId.getValue());
        config.setPassword(properties.userPassword.password.getValue());
        config.setAuthEndpoint(properties.url.getValue());

        // Notes on how to test this
        // http://thysmichels.com/2014/02/15/salesforce-wsc-partner-connection-session-renew-when-session-timeout/

        config.setSessionRenewer(new SessionRenewer() {

            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                SessionRenewalHeader header = new SessionRenewalHeader();
                try {
                    doConnection(properties, connectorConfig);
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

        if (properties.timeout.getValue() > 0) {
            config.setConnectionTimeout(properties.timeout.getValue());
        }
        if (properties.needCompression.isValueTrue()) {
            config.setCompression(properties.needCompression.getValue());
        }

        config.setTraceMessage(true);

        doConnection(properties, config);

        System.out.println("Connection: " + connection);
        System.out.println("Bulk Connection: " + bulkConnection);
    }

    public List<String> getModuleNames() throws ConnectionException {
        List<String> returnList = new ArrayList();
        DescribeGlobalResult result = connection.describeGlobal();
        DescribeGlobalSObjectResult[] objects = result.getSobjects();
        for (DescribeGlobalSObjectResult obj : objects) {
            System.out.println("module label: " + obj.getLabel() + " name: " + obj.getName());
            returnList.add(obj.getName());
        }
        return returnList;
    }

    public void setupSchemaElement(Field field, ComponentSchemaElement element) {
        String type = field.getType().toString();
        if (type.equals("boolean")) { //$NON-NLS-1$
            element.setType(ComponentSchemaElement.Type.BOOLEAN);
        } else if (type.equals("int")) { //$NON-NLS-1$
            element.setType(ComponentSchemaElement.Type.INT);
        } else if (type.equals("date")) { //$NON-NLS-1$
            element.setType(ComponentSchemaElement.Type.DATE);
            element.setPattern("\"yyyy-MM-dd\""); //$NON-NLS-1$
        } else if (type.equals("datetime")) { //$NON-NLS-1$
            element.setType(ComponentSchemaElement.Type.DATETIME);
            element.setPattern("\"yyyy-MM-dd\'T\'HH:mm:ss\'.000Z\'\""); //$NON-NLS-1$
        } else if (type.equals("double")) { //$NON-NLS-1$
            element.setType(ComponentSchemaElement.Type.DOUBLE);
        } else if (type.equals("currency")) { //$NON-NLS-1$
            element.setType(ComponentSchemaElement.Type.DECIMAL);
        }
        element.setNullable(field.getNillable());

        if (element.getType() == ComponentSchemaElement.Type.STRING) {
            element.setSize(field.getLength());
            element.setPrecision(field.getPrecision());
        } else {
            element.setSize(field.getPrecision());
            element.setPrecision(field.getScale());
        }
        element.setDefaultValue(field.getDefaultValueFormula());
    }

    public ComponentSchema getSchema(String module) throws ConnectionException {
        ComponentSchema schema = ComponentSchemaFactory.getComponentSchema();
        ComponentSchemaElement root = ComponentSchemaFactory.getComponentSchemaElement("Root");
        schema.setRoot(root);

        DescribeSObjectResult[] describeSObjectResults = connection.describeSObjects(new String[] { module });
        Field fields[] = describeSObjectResults[0].getFields();
        for (Field field : fields) {
            ComponentSchemaElement child = ComponentSchemaFactory.getComponentSchemaElement(field.getName());
            setupSchemaElement(field, child);
            root.addChild(child);
        }
        return schema;
    }

    public void commonBegin(ComponentProperties props, ComponentRuntimeContainer env) {

    }

    public void input(ComponentProperties props, ComponentRuntimeContainer container, List<Map<String, Object>> values)
            throws Exception {
        inputBegin(props, container, values);
        inputEnd(props, container, values);
    }

    public void inputBegin(ComponentProperties props, ComponentRuntimeContainer container, List<Map<String, Object>> values)
            throws Exception {
        TSalesforceInputProperties sprops = (TSalesforceInputProperties) props;

        // FIXME - refactor in common having InputOutput properties
        fieldMap = sprops.module.schema.schema.getValue().getRoot().getChildMap();
        fieldList = sprops.module.schema.schema.getValue().getRoot().getChildren();

        for (ComponentSchemaElement se : fieldList) {
            if (se.getType() == ComponentSchemaElement.Type.DYNAMIC) {
                dynamicField = se;
                break;
            }
        }

        /*
         * Dynamic columns are requested, find them from Salesforce and only look at the ones that are not explicitly
         * specified in the schema.
         */
        if (dynamicField != null) {
            List<ComponentSchemaElement> filteredDynamicFields = new ArrayList();
            ComponentSchema schema = getSchema(sprops.module.moduleName.getValue());

            for (ComponentSchemaElement se : schema.getRoot().getChildren()) {
                if (fieldMap.containsKey(se.getName()))
                    continue;
                filteredDynamicFields.add(se);
            }
            dynamicFieldList = filteredDynamicFields;
            container.setDynamicElements(dynamicFieldList.toArray(new ComponentSchemaElement[] {}));
        }

        inputFieldsToUse = new ArrayList();
        inputFieldsToUse.addAll(fieldList);
        inputFieldsToUse.addAll(dynamicFieldList);

        String queryText;
        if (sprops.manualQuery.isValueTrue()) {
            queryText = sprops.query.getValue();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            int count = 0;
            for (ComponentSchemaElement se : inputFieldsToUse) {
                if (count++ > 0)
                    sb.append(", ");
                sb.append(se.getName());
            }
            sb.append(" from ");
            sb.append(sprops.module.moduleName.getValue());
            queryText = sb.toString();
        }

        QueryResult result = query(queryText, sprops.batchSize.getValue());

    }

    public void inputEnd(ComponentProperties props, ComponentRuntimeContainer container, List<Map<String, Object>> values)
            throws Exception {
    }

    public void output(ComponentProperties props, ComponentRuntimeContainer container, List<Map<String, Object>> values)
            throws Exception {
        outputBegin(props, container);
        outputMain(props, container, values);
        outputEnd(props, container);
    }

    public void outputBegin(ComponentProperties props, ComponentRuntimeContainer env) {
        TSalesforceOutputProperties sprops = (TSalesforceOutputProperties) props;

        upsertKeyColumn = sprops.upsertKeyColumn.getValue();
        fieldMap = sprops.module.schema.schema.getValue().getRoot().getChildMap();
        fieldList = sprops.module.schema.schema.getValue().getRoot().getChildren();

        for (ComponentSchemaElement se : fieldList) {
            if (se.getType() == ComponentSchemaElement.Type.DYNAMIC) {
                dynamicField = se;
            }
        }
    }

    /**
     *
     * Process one or more rows to be output to Salesforce.
     * 
     * @param props
     * @param env
     * @param rows a {@link List} of {@link Map} objects. Each object in the List is a row to be processed, in a DI
     * container this would correspond to a connection.
     */
    public void outputMain(ComponentProperties props, ComponentRuntimeContainer env, List<Map<String, Object>> rows)
            throws Exception {
        TSalesforceOutputProperties sprops = (TSalesforceOutputProperties) props;
        for (Map<String, Object> row : rows) {
            if (sprops.outputAction.getValue() != TSalesforceOutputProperties.OutputAction.DELETE) {
                SObject so = new SObject();
                so.setType(sprops.module.moduleName.getValue());

                for (String key : row.keySet()) {
                    Object value = row.get(key);
                    if (value != null) {
                        ComponentSchemaElement se = fieldMap.get(key);
                        if (se != null)
                            addSObjectField(so, se, value);
                    }
                }

                if (dynamicField != null) {
                    Object dynamic = row.get(dynamicField.getName());
                    ComponentSchemaElement[] dynamicSes = container.getDynamicElements(dynamic);
                    for (ComponentSchemaElement dynamicSe : dynamicSes) {
                        Object value = container.getDynamicValue(dynamic, dynamicSe.getName());
                        addSObjectField(so, dynamicSe, value);
                    }
                }

                switch (sprops.outputAction.getValue()) {
                case INSERT:
                    insert(so);
                    break;
                case UPDATE:
                    update(so);
                    break;
                case UPSERT:
                    upsert(so);
                    break;
                }
            } else { // DELETE
                String id = getIdValue(row);
                if (id != null)
                    delete(id);
            }
        }
    }

    public void outputEnd(ComponentProperties props, ComponentRuntimeContainer env) throws Exception {
        logout();
    }

    protected String getIdValue(Map<String, Object> row) {
        String ID = "Id";
        if (row.get(ID) != null) {
            ComponentSchemaElement se = fieldMap.get(ID);
            if (se.getType() != ComponentSchemaElement.Type.DYNAMIC) {
                return (String) row.get(ID);
            }
        }
        // FIXME - error?
        if (dynamicField == null)
            return null;

        Object dynamic = row.get(dynamicField.getName());
        ComponentSchemaElement[] dynamicSes = container.getDynamicElements(dynamic);
        for (ComponentSchemaElement dynamicSe : dynamicSes) {
            if (dynamicSe.getName().equals(ID)) {
                return (String) container.getDynamicValue(dynamic, ID);
            }
        }
        // FIXME - really an error
        return null;
    }

    protected void addSObjectField(SObject sObject, ComponentSchemaElement se, Object value) {
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

    public void logout() throws Exception {
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

    public DeleteResult[] delete(String id) throws Exception {
        if (id == null)
            return null;
        deleteItems.add(id);
        return doDelete();
    }

    private DeleteResult[] doDelete() throws Exception {
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

    public SaveResult[] insert(SObject sObject) throws Exception {
        insertItems.add(sObject);
        return doInsert();
    }

    private SaveResult[] doInsert() throws Exception {
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

    public SaveResult[] update(SObject sObject) throws Exception {
        updateItems.add(sObject);
        return doUpdate();
    }

    private SaveResult[] doUpdate() throws Exception {
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

    public UpsertResult[] upsert(SObject sObject) throws Exception {
        upsertItems.add(sObject);
        return doUpsert();
    }

    private UpsertResult[] doUpsert() throws Exception {
        if (upsertItems.size() >= commitLevel) {
            SObject[] upds = upsertItems.toArray(new SObject[upsertItems.size()]);
            String[] changedItemKeys = new String[upds.length];
            for (int ix = 0; ix < upds.length; ++ix) {
                Object value = upds[ix].getField(upsertKeyColumn);
                if (value == null)
                    changedItemKeys[ix] = "No value for " + upsertKeyColumn + " ";
                else
                    changedItemKeys[ix] = upsertKeyColumn;
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

    private void handleResults(boolean success, Error[] resultErrors, String[] changedItemKeys, int batchIdx) throws Exception {
        StringBuilder errors = new StringBuilder("");
        if (success) {
            // TODO: send back the ID
        } else {
            errors = addLog(resultErrors, batchIdx < changedItemKeys.length ? changedItemKeys[batchIdx]
                    : "Batch index out of bounds");
        }
        if (exceptionForErrors && errors.toString().length() > 0) {
            if (logWriter != null) {
                logWriter.close();
            }
            throw new Exception(errors.toString());
        }
    }

    private StringBuilder addLog(Error[] resultErrors, String row_key) throws Exception {
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

    public Map<String, String> readResult(Object[] results) throws Exception {
        Map<String, String> resultMessage = null;
        if (results instanceof SaveResult[]) {
            for (SaveResult result : (SaveResult[]) results) {
                resultMessage = new HashMap<String, String>();
                if (result.getId() != null) {
                    resultMessage.put("id", result.getId());
                }
                resultMessage.put("success", String.valueOf(result.getSuccess()));
                if (!result.getSuccess()) {
                    for (Error error : result.getErrors()) {
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
                    for (Error error : result.getErrors()) {
                        if (error.getStatusCode() != null) {
                            resultMessage.put("StatusCode", error.getStatusCode().toString());
                        }
                        resultMessage.put("Message", error.getMessage());
                    }
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
                    for (Error error : result.getErrors()) {
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
            }
            return resultMessage;
        }
        return null;
    }

    public String[] getUpdated(String objectType, Calendar startDate, Calendar endDate) throws Exception {
        return connection.getUpdated(objectType, startDate, endDate).getIds();
    }

    public SObject[] retrieve(String[] ids, String objectType, String fieldsList) throws Exception {
        return connection.retrieve(fieldsList, objectType, ids);
    }

    public GetDeletedResult getDeleted(String objectType, Calendar startDate, Calendar endDate) throws Exception {
        return connection.getDeleted(objectType, startDate, endDate);
    }

    public QueryResult queryAll(String soql, int batchSize) throws Exception {
        connection.setQueryOptions(batchSize);
        return connection.queryAll(soql);
    }

    public QueryResult queryMore(String queryLocator, int batchSize) throws Exception {
        connection.setQueryOptions(batchSize);
        return connection.queryMore(queryLocator);
    }

    public QueryResult query(String soql, int batchSize) throws Exception {
        connection.setQueryOptions(batchSize);
        return connection.query(soql);
    }

    public Calendar getServerTimestamp() throws ConnectionException {
        return connection.getServerTimestamp().getTimestamp();
    }

}
