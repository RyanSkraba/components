// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class SalesforceServerTimeStampReader extends AbstractBoundedReader<Long> {

    private transient Long result;

    protected int dataCount;

    protected RuntimeContainer container;

    public SalesforceServerTimeStampReader(RuntimeContainer container, SalesforceSource source,
                                           TSalesforceGetServerTimestampProperties props) {
        super(source);
        this.container = container;
    }

    @Override
    public boolean start() throws IOException {
        PartnerConnection connection = ((SalesforceSource) getCurrentSource()).connect(container).connection;
        try {
            Calendar serverTimestamp = connection.getServerTimestamp().getTimestamp();
            if (serverTimestamp != null) {
                result = serverTimestamp.getTimeInMillis();
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
    public Long getCurrent() throws NoSuchElementException {
        return result;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.totalCount = dataCount;
        return result.toMap();
    }

}
