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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

public class SalesforceSource extends SalesforceSourceOrSink implements BoundedSource {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceSource.class);

    public SalesforceSource() {
    }


    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, Adaptor adaptor) throws Exception {
        List list = new ArrayList();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(Adaptor adaptor) throws Exception {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(Adaptor adaptor) throws Exception {
        return false;
    }

    @Override
    public BoundedReader createReader(Adaptor adaptor) throws IOException {
        if (properties instanceof TSalesforceInputProperties)
            return new SalesforceInputReader(adaptor, this, (TSalesforceInputProperties) properties);
        return null;
    }
}
