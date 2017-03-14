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

import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE;
import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.runtime.client.MarketoClientService;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;

public class MarketoInputReader extends AbstractBoundedReader<IndexedRecord> {

    protected RuntimeContainer adaptor;

    protected MarketoSource source;

    protected TMarketoInputProperties properties;

    protected int apiCalls = 0;

    protected String errorMessage;

    MarketoClientService client;

    MarketoRecordResult mktoResult;

    protected List<IndexedRecord> records;

    protected int recordIndex;

    public MarketoInputReader(RuntimeContainer adaptor, MarketoSource source, TMarketoInputProperties properties) {
        super(source);
        this.adaptor = adaptor;
        this.source = source;
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable = false;
        client = source.getClientService(null);
        switch (properties.inputOperation.getValue()) {
        case getLead:
            mktoResult = client.getLead(properties, null);
            break;
        case getMultipleLeads:
            mktoResult = client.getMultipleLeads(properties, null);
            break;
        case getLeadActivity:
            mktoResult = client.getLeadActivity(properties, null);
            break;
        case getLeadChanges:
            mktoResult = client.getLeadChanges(properties, null);
            break;
        case CustomObject:
            switch (properties.customObjectAction.getValue()) {
            case describe:
                mktoResult = ((MarketoRESTClient) client).describeCustomObject(properties);
                break;
            case list:
                mktoResult = ((MarketoRESTClient) client).listCustomObjects(properties);
                break;
            case get:
                mktoResult = ((MarketoRESTClient) client).getCustomObjects(properties, null);
                break;
            }
            break;
        }
        startable = mktoResult.getRecordCount() > 0;
        apiCalls++;
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
            return false;
        }
        // fetch more data
        switch (properties.inputOperation.getValue()) {
        case getLead:
            mktoResult = client.getLead(properties, mktoResult.getStreamPosition());
            break;
        case getMultipleLeads:
            mktoResult = client.getMultipleLeads(properties, mktoResult.getStreamPosition());
            break;
        case getLeadActivity:
            mktoResult = client.getLeadActivity(properties, mktoResult.getStreamPosition());
            break;
        case getLeadChanges:
            mktoResult = client.getLeadChanges(properties, mktoResult.getStreamPosition());
            break;
        case CustomObject:
            switch (properties.customObjectAction.getValue()) {
            case describe:
                mktoResult = ((MarketoRESTClient) client).describeCustomObject(properties);
                break;
            case list:
                mktoResult = ((MarketoRESTClient) client).listCustomObjects(properties);
                break;
            case get:
                mktoResult = ((MarketoRESTClient) client).getCustomObjects(properties, mktoResult.getStreamPosition());
                break;
            }
            break;
        }
        boolean advanceable = mktoResult.getRecordCount() > 0;
        apiCalls++;
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
        result.totalCount = apiCalls;
        Map<String, Object> res = result.toMap();
        res.put(RETURN_NB_CALL, apiCalls);
        res.put(RETURN_ERROR_MESSAGE, errorMessage);
        return res;
    }

}
