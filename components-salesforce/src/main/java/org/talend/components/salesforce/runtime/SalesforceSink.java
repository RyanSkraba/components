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
import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;

public class SalesforceSink extends SalesforceSourceOrSink implements Sink {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceSink.class);

    public SalesforceSink() {
    }

    @Override
    public WriteOperation<?> createWriteOperation(Adaptor adaptor) {
        return new SalesforceWriteOperation(this);
    }
}
