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
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.runtime.common.ConnectionHolder;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class SalesforceServerTimeStampReader extends AbstractBoundedReader<IndexedRecord> {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(SalesforceServerTimeStampReader.class);

    private static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SalesforceServerTimeStampReader.class);

    private transient IndexedRecord result;

    protected int dataCount;

    protected RuntimeContainer container;

    protected TSalesforceGetServerTimestampProperties properties;

    public SalesforceServerTimeStampReader(RuntimeContainer container, SalesforceSource source,
            TSalesforceGetServerTimestampProperties properties) {
        super(source);
        this.container = container;
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        ConnectionHolder ch = ((SalesforceSource) getCurrentSource()).connect(container);
        PartnerConnection connection = ch.connection;
        if (ch.bulkConnection != null) {
            LOGGER.info(MESSAGES.getMessage("info.bulkConnectionUsage"));
        }
        try {
            Calendar serverTimestamp = connection.getServerTimestamp().getTimestamp();
            if (serverTimestamp != null) {
                long timestamp = serverTimestamp.getTimeInMillis();
                result = new GenericData.Record(properties.schema.schema.getValue());
                result.put(0, timestamp);
            }
            if (result != null) {
                dataCount++;
                return true;
            } else {
                return false;
            }
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        return false;// only one record is available for this reader.
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return result;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.totalCount = dataCount;
        return result.toMap();
    }

}
