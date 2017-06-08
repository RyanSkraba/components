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

package org.talend.components.netsuite.output;

import java.util.List;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NsWriteResponse;

/**
 * Performs bulk <code>update</code> operation.
 *
 * @param <T> type of NetSuite objects that are passed to {@link NetSuiteClientService}
 * @param <RefT> type of NetSuite reference objects
 */
public class NetSuiteUpdateWriter<T, RefT> extends NetSuiteOutputWriter<T, RefT> {

    public NetSuiteUpdateWriter(NetSuiteWriteOperation writeOperation, RuntimeContainer container, MetaDataSource metaDataSource) {
        super(writeOperation, container, metaDataSource);
    }

    @Override
    protected List<NsWriteResponse<RefT>> doWrite(List<T> nsObjectList) {
        return clientService.updateList(nsObjectList);
    }
}
