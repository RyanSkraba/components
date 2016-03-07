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

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;

import com.sforce.ws.ConnectionException;

public class SalesforceServerTimeStampReader extends SalesforceReader<Calendar> {

    private TSalesforceGetServerTimestampProperties props;

    private Calendar result;

    private Schema schema;

    public SalesforceServerTimeStampReader(RuntimeContainer adaptor, SalesforceSource source,
            TSalesforceGetServerTimestampProperties props) {
        super(adaptor, source);
        this.props = props;
    }

    @Override
    public boolean start() throws IOException {
        super.start();
        TSalesforceGetServerTimestampProperties gdProps = props;
        schema = new Schema.Parser().parse(gdProps.schema.schema.getStringValue());
        try {
            result = connection.getServerTimestamp().getTimestamp();
            return result != null;
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        return false;// only one record is avalable for this reader.
    }

    @Override
    public Calendar getCurrent() throws NoSuchElementException {
        return result;
    }

}
