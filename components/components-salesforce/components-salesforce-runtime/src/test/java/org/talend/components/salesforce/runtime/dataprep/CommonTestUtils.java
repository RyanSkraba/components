package org.talend.components.salesforce.runtime.dataprep;

import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;

public class CommonTestUtils {

    public static void setValueForDatastoreProperties(SalesforceDatastoreProperties datastore) {
        datastore.userId.setValue(System.getProperty("salesforce.user"));
        datastore.password.setValue(System.getProperty("salesforce.password"));
        datastore.securityKey.setValue(System.getProperty("salesforce.key"));
    }
    
}
