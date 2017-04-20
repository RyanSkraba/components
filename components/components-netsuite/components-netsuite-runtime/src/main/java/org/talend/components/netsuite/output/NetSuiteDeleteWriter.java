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
import org.talend.components.netsuite.client.NsWriteResponse;

/**
 * Performs bulk <code>delete</code> operation.
 *
 * <p>This processor calls <code>deleteList</code> operation and writes reference objects.
 *
 * @param <RefT> type of NetSuite reference objects
 */
public class NetSuiteDeleteWriter<RefT> extends NetSuiteOutputWriter<RefT, RefT> {

    public NetSuiteDeleteWriter(NetSuiteWriteOperation writeOperation, MetaDataSource metaDataSource) {
        super(writeOperation, metaDataSource);
    }

    @Override
    protected void initTransducer() {
        super.initTransducer();

        transducer.setReference(true);
    }

    @Override
    protected List<NsWriteResponse<RefT>> doWrite(List<RefT> nsObjectList) {
        return clientService.deleteList(nsObjectList);
    }
}
