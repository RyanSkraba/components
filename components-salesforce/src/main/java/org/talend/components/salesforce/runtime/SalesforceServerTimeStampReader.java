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
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.Calendar;
import java.util.NoSuchElementException;

import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class SalesforceServerTimeStampReader extends AbstractBoundedReader<Long> {

    private transient Long result;

    protected int dataCount;

    public SalesforceServerTimeStampReader(RuntimeContainer container, SalesforceSource source,
                                           TSalesforceGetServerTimestampProperties props) {
        super(container, source);
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
    public void close() throws IOException {
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), TSalesforceGetServerTimestampProperties.NB_LINE_NAME, dataCount);
        }
    }

}
