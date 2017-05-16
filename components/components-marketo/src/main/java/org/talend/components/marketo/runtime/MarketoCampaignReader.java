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
import static org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction.get;
import static org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction.getById;
import static org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction.schedule;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class MarketoCampaignReader extends AbstractBoundedReader<IndexedRecord> {

    private MarketoSource source;

    private TMarketoCampaignProperties properties;

    private int apiCalls = 0;

    private MarketoRESTClient client;

    private String errorMessage;

    private MarketoRecordResult mktoResult;

    private List<IndexedRecord> records;

    private int recordIndex;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoCampaignReader.class);

    private static final Logger LOG = LoggerFactory.getLogger(MarketoCampaignReader.class);

    public MarketoCampaignReader(RuntimeContainer adaptor, MarketoSource source, TMarketoCampaignProperties properties) {
        super(source);
        this.source = source;
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable;
        client = (MarketoRESTClient) source.getClientService(null);
        mktoResult = executeOperation(null);
        apiCalls++;
        if (!mktoResult.isSuccess()) {
            throw new IOException(mktoResult.getErrorsString());
        }
        startable = mktoResult.getRecordCount() > 0;
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
        apiCalls++;
        mktoResult = executeOperation(mktoResult.getStreamPosition());
        if (!mktoResult.isSuccess()) {
            throw new IOException(mktoResult.getErrorsString());
        }
        boolean advanceable = mktoResult.getRecordCount() > 0;
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

    public MarketoRecordResult executeOperation(String position) throws IOException {
        if (properties.campaignAction.getValue().equals(get)) {
            return client.getCampaigns(properties, position);
        } else if (properties.campaignAction.getValue().equals(getById)) {
            return client.getCampaignById(properties);
        } else if (properties.campaignAction.getValue().equals(schedule)) {
            return client.scheduleCampaign(properties);
        }
        throw new IOException(messages.getMessage("error.reader.invalid.operation"));
    }

}
