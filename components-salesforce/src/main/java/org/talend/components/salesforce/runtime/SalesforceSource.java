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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforcegetdeleted.TSalesforceGetDeletedProperties;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;
import org.talend.components.salesforce.tsalesforcegetupdated.TSalesforceGetUpdatedProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import java.util.ArrayList;
import java.util.List;

public class SalesforceSource extends SalesforceSourceOrSink implements BoundedSource {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceSource.class);

    public SalesforceSource() {
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        if (properties instanceof TSalesforceInputProperties) {
            TSalesforceInputProperties sfInProperties = (TSalesforceInputProperties) properties;
            boolean isBulk = TSalesforceInputProperties.QueryMode.BULK.equals(sfInProperties.queryMode.getStringValue());
            sfInProperties.connection.bulkConnection.setValue(isBulk);
            if(isBulk){
                return new SalesforceBulkQueryInputReader(adaptor, this, sfInProperties);
            }else{
                return new SalesforceInputReader(adaptor, this, sfInProperties);
            }
        } else if (properties instanceof TSalesforceGetServerTimestampProperties) {
            return new SalesforceServerTimeStampReader(adaptor, this, (TSalesforceGetServerTimestampProperties) properties);
        } else if (properties instanceof TSalesforceGetDeletedProperties) {
            return new SalesforceGetDeletedReader(adaptor, this, (TSalesforceGetDeletedProperties) properties);
        } else if (properties instanceof TSalesforceGetUpdatedProperties) {
            return new SalesforceGetUpdatedReader(adaptor, this, (TSalesforceGetUpdatedProperties) properties);
        }else if (properties instanceof TSalesforceBulkExecProperties) {
            return new SalesforceBulkExecReader(adaptor, this, (TSalesforceBulkExecProperties) properties);
        }
        return null;
    }
}
