package org.talend.components.salesforce.runtime.common;

import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.PartnerConnection;

public final class ConnectionHolder {

    public PartnerConnection connection;

    public BulkConnection bulkConnection;
    
}