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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NsWriteResponse;
import org.talend.components.netsuite.client.model.beans.Beans;

/**
 * Performs bulk <code>upsert</code> operation.
 *
 * @param <T> type of NetSuite objects that are passed to {@link NetSuiteClientService}
 * @param <RefT> type of NetSuite reference objects
 */
public class NetSuiteUpsertWriter<T, RefT> extends NetSuiteOutputWriter<T, RefT> {

    /**
     * Specifies writer should use native <code>upsert</code> operation or
     * should use <code>add</code> and <code>update</code> for new and existing records correspondingly.
     */
    private boolean useNativeUpsert;

    public NetSuiteUpsertWriter(NetSuiteWriteOperation writeOperation, MetaDataSource metaDataSource) {
        super(writeOperation, metaDataSource);
    }

    public boolean isUseNativeUpsert() {
        return useNativeUpsert;
    }

    public void setUseNativeUpsert(boolean useNativeUpsert) {
        this.useNativeUpsert = useNativeUpsert;
    }

    @Override
    protected List<NsWriteResponse<RefT>> doWrite(List<T> nsObjectList) {
        if (useNativeUpsert) {
            return doNativeUpsert(nsObjectList);
        } else {
            return doCustomUpsert(nsObjectList);
        }
    }

    protected List<NsWriteResponse<RefT>> doNativeUpsert(List<T> nsObjectList) {
        return clientService.upsertList(nsObjectList);
    }

    protected List<NsWriteResponse<RefT>> doCustomUpsert(List<T> nsObjectList) {
        // Create separate list for adding and updating of NetSuite objects

        List<T> addList = null;
        List<T> updateList = null;
        for (T nsObject : nsObjectList) {
            String internalId = (String) Beans.getSimpleProperty(nsObject, "internalId");
            String externalId = (String) Beans.getSimpleProperty(nsObject, "externalId");
            if (StringUtils.isNotEmpty(internalId) || StringUtils.isNotEmpty(externalId)) {
                if (updateList == null) {
                    updateList = new ArrayList<>();
                }
                updateList.add(nsObject);
            } else {
                if (addList == null) {
                    addList = new ArrayList<>();
                }
                addList.add(nsObject);
            }
        }

        // Perform adding and updating of objects and collect write responses

        Map<T, NsWriteResponse<RefT>> responseMap = new HashMap<>(nsObjectList.size());

        if (addList != null) {
            List<NsWriteResponse<RefT>> responseList = clientService.addList(addList);
            for (int i = 0; i < addList.size(); i++) {
                T nsObject = addList.get(i);
                NsWriteResponse<RefT> response = responseList.get(i);
                responseMap.put(nsObject, response);
            }
        }

        if (updateList != null) {
            List<NsWriteResponse<RefT>> responseList = clientService.updateList(updateList);
            for (int i = 0; i < updateList.size(); i++) {
                T nsObject = updateList.get(i);
                NsWriteResponse<RefT> response = responseList.get(i);
                responseMap.put(nsObject, response);
            }
        }

        // Create combined list of write responses

        List<NsWriteResponse<RefT>> responseList = new ArrayList<>(nsObjectList.size());
        for (T nsObject : nsObjectList) {
            NsWriteResponse<RefT> response = responseMap.get(nsObject);
            responseList.add(response);
        }
        return responseList;
    }
}
