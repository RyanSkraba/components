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

import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NsWriteResponse;

/**
 * Performs bulk <code>add</code> operation.
 *
 * <p>This processor calls <code>addList</code> operation.
 *
 * @param <T> type of NetSuite objects that are passed to {@link NetSuiteClientService}
 * @param <RefT> type of NetSuite reference objects
 */
public class NetSuiteAddWriter<T, RefT> extends NetSuiteOutputWriter<T, RefT> {

    public NetSuiteAddWriter(NetSuiteWriteOperation writeOperation, MetaDataSource metaDataSource) {
        super(writeOperation, metaDataSource);
    }

    @Override
    protected List<NsWriteResponse<RefT>> doWrite(List<T> nsObjectList) {
        return clientService.addList(nsObjectList);
    }
}
