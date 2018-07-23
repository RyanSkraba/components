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
package org.talend.components.marketo.runtime;

import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE;
import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.runtime.client.MarketoClientService;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsREST;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class MarketoInputReader extends AbstractBoundedReader<IndexedRecord> {

    private MarketoSource source;

    private TMarketoInputProperties properties;

    private int apiCalls = 0;

    private String errorMessage;

    private MarketoClientService client;

    private MarketoRecordResult mktoResult;

    private List<IndexedRecord> records;

    private int recordIndex;

    private Boolean isDynamic = Boolean.FALSE;

    private Boolean useActivitiesList = Boolean.FALSE;

    private List<List<String>> activities;

    private int activitiesListIndex = 0;

    protected int retryAttemps = 1;

    protected int retryInterval;

    private static final Logger LOG = LoggerFactory.getLogger(MarketoInputReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoInputReader.class);

    public static <T> List<List<T>> splitList(List<T> objList, int subSize) {
        int listMaxSize = objList.size() % subSize == 0 ? objList.size() / subSize : objList.size() / subSize + 1;
        List<List<T>> returnList = new ArrayList<>();
        for (int i = 0; i < listMaxSize; i++) {
            returnList.add(new ArrayList<T>());
        }
        for (int index = 0; index < objList.size(); index++) {
            returnList.get(index / subSize).add(objList.get(index));
        }
        return returnList;
    }

    public MarketoInputReader(RuntimeContainer adaptor, MarketoSource source, TMarketoInputProperties properties) {
        super(source);
        this.source = source;
        this.properties = properties;
        // check if we've a dynamic schema...
        isDynamic = AvroUtils.isIncludeAllFields(this.properties.schemaInput.schema.getValue());
        if (properties.isApiREST() && InputOperation.getLeadActivity.equals(properties.inputOperation.getValue())) {
            useActivitiesList = true;
            // include all activities
            List<String> tmp;
            if (!properties.setIncludeTypes.getValue()) {
                tmp = new ArrayList<>();
                for (IncludeExcludeFieldsREST a : IncludeExcludeFieldsREST.values()) {
                    tmp.add(a.name());
                }
            } else {
                tmp = properties.includeTypes.type.getValue();
            }
            if (properties.setExcludeTypes.getValue()) {
                tmp.removeAll(properties.excludeTypes.type.getValue());
            }
            activities = splitList(tmp, 10);
            LOG.debug("activities to process = {}.", activities);
        }

        retryAttemps = this.properties.getConnectionProperties().maxReconnAttemps.getValue();
        retryInterval = this.properties.getConnectionProperties().attemptsIntervalTime.getValue();
    }

    public void adaptSchemaToDynamic() throws IOException {
        Schema design = this.properties.schemaInput.schema.getValue();
        if (!isDynamic) {
            return;
        }
        try {
            Schema runtimeSchema;
            if (!properties.inputOperation.getValue().equals(InputOperation.CustomObject)) {
                runtimeSchema = source.getDynamicSchema("", design);
                // preserve mappings to re-apply them after
                Map<String, String> mappings = properties.mappingInput.getNameMappingsForMarketo();
                List<String> columnNames = new ArrayList<>();
                List<String> mktoNames = new ArrayList<>();
                for (Field f : runtimeSchema.getFields()) {
                    columnNames.add(f.name());
                    if (mappings.get(f.name()) != null) {
                        mktoNames.add(mappings.get(f.name()));
                    } else {
                        mktoNames.add("");
                    }
                }
                properties.mappingInput.columnName.setValue(columnNames);
                properties.mappingInput.marketoColumnName.setValue(mktoNames);
            } else {
                runtimeSchema = source.getDynamicSchema(properties.customObjectName.getValue(), design);
            }
            properties.schemaInput.schema.setValue(runtimeSchema);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    public MarketoRecordResult executeOperation(String position) throws IOException {
        MarketoRecordResult mkto = new MarketoRecordResult();
        for (int i = 0; i < getRetryAttemps(); i++) {
            apiCalls++;
            switch (properties.inputOperation.getValue()) {
            case getLead:
                if (isDynamic) {
                    adaptSchemaToDynamic();
                }
                mkto = client.getLead(properties, position);
                break;
            case getMultipleLeads:
                if (isDynamic) {
                    adaptSchemaToDynamic();
                }
                mkto = client.getMultipleLeads(properties, position);
                break;
            case getLeadActivity:
                mkto = client.getLeadActivity(properties, position);
                break;
            case getLeadChanges:
                mkto = client.getLeadChanges(properties, position);
                break;
            case CustomObject:
                switch (properties.customObjectAction.getValue()) {
                case describe:
                    mkto = ((MarketoRESTClient) client).describeCustomObject(properties);
                    break;
                case list:
                    mkto = ((MarketoRESTClient) client).listCustomObjects(properties);
                    break;
                case get:
                    if (isDynamic) {
                        adaptSchemaToDynamic();
                    }
                    mkto = ((MarketoRESTClient) client).getCustomObjects(properties, position);
                    break;
                default:
                    throw new IOException(messages.getMessage("error.reader.invalid.operation"));
                }
                break;
            case Company:
                switch (properties.standardAction.getValue()) {
                case describe:
                    mkto = ((MarketoRESTClient) client).describeCompanies(properties);
                    break;
                case get:
                    mkto = ((MarketoRESTClient) client).getCompanies(properties, position);
                    break;
                }
                break;
            case Opportunity:
            case OpportunityRole:
                switch (properties.standardAction.getValue()) {
                case describe:
                    mkto = ((MarketoRESTClient) client).describeOpportunity(properties);
                    break;
                case get:
                    mkto = ((MarketoRESTClient) client).getOpportunities(properties, position);
                    break;
                }
                break;
            default:
                throw new IOException(messages.getMessage("error.reader.invalid.operation"));
            }
            //
            if (!mkto.isSuccess()) {
                if (properties.dieOnError.getValue()) {
                    throw new MarketoRuntimeException(mkto.getErrorsString());
                }
                // is recoverable error
                if (client.isErrorRecoverable(mkto.getErrors())) {
                    LOG.debug("Recoverable error during operation : `{}`. Retrying...", mkto.getErrorsString());
                    waitForRetryAttempInterval();
                    continue;
                } else {
                    LOG.error("Unrecoverable error : `{}`.", mkto.getErrorsString());
                    break;
                }
            } else {
                break;
            }
        }
        return mkto;
    }

    public boolean checkResult(MarketoRecordResult mkto) {
        boolean result;
        if (!mkto.isSuccess()) {
            result = false;
        } else {
            result = mktoResult.getRecordCount() > 0;
        }
        return result;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable;
        client = source.getClientService(null);
        if (useActivitiesList) {
            if (activities.size() == 0) {
                throw new IOException(messages.getMessage("error.runtime.leadactivity.activities.empty"));
            }
            properties.includeTypes.type.setValue(activities.get(activitiesListIndex++));
        }
        mktoResult = executeOperation(null);
        startable = checkResult(mktoResult);
        // check for activities, first batch may be empty
        if (!startable && mktoResult.isSuccess() && useActivitiesList && activitiesListIndex != activities.size()) {
            while (activitiesListIndex != activities.size()) {
                properties.includeTypes.type.setValue(activities.get(activitiesListIndex++));
                mktoResult = executeOperation(null);
                startable = checkResult(mktoResult);
                if (startable) {
                    break;
                }
            }
        }
        //
        if (startable) {
            records = mktoResult.getRecords();
            recordIndex = 0;
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        recordIndex++;
        if (recordIndex < records.size()) {
            return true;
        }
        if (mktoResult.getRemainCount() == 0) {
            if (useActivitiesList) {
                // we have processed all activities
                if (activitiesListIndex == activities.size()) {
                    return false;
                }
                properties.includeTypes.type.setValue(activities.get(activitiesListIndex++));
                mktoResult.setStreamPosition(null);
            } else {
                return false;
            }
        }
        // fetch more data
        mktoResult = executeOperation(mktoResult.getStreamPosition());
        boolean advanceable = checkResult(mktoResult);
        if (!advanceable && mktoResult.isSuccess() && useActivitiesList && activitiesListIndex != activities.size()) {
            while (activitiesListIndex != activities.size()) {
                properties.includeTypes.type.setValue(activities.get(activitiesListIndex++));
                mktoResult = executeOperation(mktoResult.getStreamPosition());
                advanceable = checkResult(mktoResult);
                if (advanceable) {
                    break;
                }
            }
        }
        if (advanceable) {
            records = mktoResult.getRecords();
            recordIndex = 0;
        }
        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return records.get(recordIndex);
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        Map<String, Object> res = result.toMap();
        res.put(RETURN_NB_CALL, apiCalls);
        res.put(RETURN_ERROR_MESSAGE, errorMessage);
        return res;
    }

    public int getRetryAttemps() {
        return retryAttemps;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * Sleeps for retryInterval time
     *
     */
    protected void waitForRetryAttempInterval() {
        try {
            Thread.sleep(getRetryInterval());
        } catch (InterruptedException e) {
        }
    }

}
