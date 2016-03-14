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

import org.talend.components.api.component.runtime.AbstractBoundedReader;

import com.sforce.soap.partner.PartnerConnection;

public abstract class SalesforceReader<T> extends AbstractBoundedReader<T> {

    private transient PartnerConnection connection;

    public SalesforceReader(SalesforceSource source) {
        super(source);
    }

    protected PartnerConnection getConnection() throws IOException {
        if (connection == null) {
            connection = ((SalesforceSource) getCurrentSource()).connect().connection;
        }
        return connection;
    }
}
