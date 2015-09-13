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

import java.util.*;

import javax.xml.namespace.QName;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentEnvironmentContext;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.api.runtime.ComponentRuntimeContext;
import org.talend.components.api.schema.ComponentSchema;
import org.talend.components.api.schema.ComponentSchemaElement;
import org.talend.components.api.schema.ComponentSchemaFactory;
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

    PartnerConnection                   connection;

    private BulkConnection              bulkConnection;

    private ComponentEnvironmentContext context;

    private boolean                     exceptionForErrors = false;

    private java.io.BufferedWriter      logWriter          = null;

    private int                         commitLevel        = 1;

    private List<String>                deleteItems;

    private List<SObject>               insertItems;

    private List<SObject>               upsertItems;

    private List<SObject>               updateItems;

    private String                      upsertKeyColumn;

    public SalesforceRuntime() {
        this.commitLevel = 1;
        int arraySize = commitLevel * 2;
        this.deleteItems = new ArrayList<String>(arraySize);
        this.insertItems = new ArrayList<SObject>(arraySize);
        this.updateItems = new ArrayList<SObject>(arraySize);
        this.upsertItems = new ArrayList<SObject>(arraySize);
        this.upsertKeyColumn = "";
    }


    public SalesforceRuntime(ComponentEnvironmentContext context) {
        this();
        this.context = context;
    }

    public SalesforceRuntime(ComponentEnvironmentContext context, int commitLevel, boolean exceptionForErrors, String errorLogFile)
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
        if (properties.bulkConnection.getValue() != null && properties.bulkConnection.getValue()) {
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
        if (properties.needCompression.getValue() != null) {
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

    public ComponentSchema getSchema(String module) throws ConnectionException {
        ComponentSchema schema = ComponentSchemaFactory.getComponentSchema();
        ComponentSchemaElement root = ComponentSchemaFactory.getComponentSchemaElement("Root");
        schema.setRoot(root);

        DescribeSObjectResult[] describeSObjectResults = connection.describeSObjects(new String[] { module });
        Field fields[] = describeSObjectResults[0].getFields();
        for (Field field : fields) {
            ComponentSchemaElement child = ComponentSchemaFactory.getComponentSchemaElement(field.getName());
            root.addChild(child);
        }
        return schema;
    }

    class SalesforceRuntimeContext implements ComponentRuntimeContext {
    }

    public void output(ComponentProperties props, ComponentEnvironmentContext env, List<Map<String, Object>> values) {
        ComponentRuntimeContext rc = outputBegin(props, env);
        outputMain(props, rc, env, values);
        outputEnd(props, rc, env);
    }

    public ComponentRuntimeContext outputBegin(ComponentProperties props, ComponentEnvironmentContext env) {
        TSalesforceOutputProperties sprops = (TSalesforceOutputProperties) props;

        upsertKeyColumn = sprops.upsertKeyColumn.getValue();

        SalesforceRuntimeContext context = new SalesforceRuntimeContext();

        return context;
    }

    /**
     *
     * Process one or more rows to be output to Salesforce.
     * 
     * @param props
     * @param context
     * @param env
     * @param values a {@link List} of {@link Map} objects. Each object in the List is a row to be processed, in a DI
     * context this would correspond to a connection.
     */
    public void outputMain(ComponentProperties props, ComponentRuntimeContext context, ComponentEnvironmentContext env,
            List<Map<String, Object>> values) {
        TSalesforceOutputProperties sprops = (TSalesforceOutputProperties) props;
        for (Map<String, Object> connection : values) {
            switch (sprops.outputAction.getValue()) {
            case INSERT:
                break;
            case UPDATE:
                break;
            case UPSERT:
                break;
            case DELETE:
                break;
            }
        }
    }

    public void outputEnd(ComponentProperties props, ComponentRuntimeContext context, ComponentEnvironmentContext env) {

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
        GetUpdatedResult result = connection.getUpdated(objectType, startDate, endDate);
        return result.getIds();
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
        GetServerTimestampResult result = connection.getServerTimestamp();
        return result.getTimestamp();
    }

}
