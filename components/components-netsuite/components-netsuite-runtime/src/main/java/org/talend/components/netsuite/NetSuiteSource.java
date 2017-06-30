// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.input.NetSuiteInputProperties;
import org.talend.components.netsuite.input.NetSuiteSearchInputReader;

/**
 * Base class for NetSuite sources.
 */
public class NetSuiteSource extends NetSuiteSourceOrSink implements BoundedSource {

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer container)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        if (properties instanceof NetSuiteInputProperties) {
            NetSuiteInputProperties nsProperties = (NetSuiteInputProperties) properties;
            return new NetSuiteSearchInputReader(container, this, nsProperties);
        }
        throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                NetSuiteRuntimeI18n.MESSAGES.getMessage("error.invalidComponentPropertiesClass",
                        NetSuiteInputProperties.class, properties.getClass()));
    }

}
