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
package org.talend.components.marketo.data;

import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;

public class MarketoDatastoreProperties extends TMarketoConnectionProperties implements DatastoreProperties {

    public MarketoDatastoreProperties(String name) {
        super(name);
    }
}
