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

package org.talend.components.netsuite.v2016_2.client;

import static org.talend.components.netsuite.v2016_2.client.NetSuiteClientServiceImpl.toNsReadResponseList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.talend.components.netsuite.client.DefaultCustomMetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsReadResponse;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.model.BasicRecordType;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RefType;

import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.core.CustomizationRef;
import com.netsuite.webservices.v2016_2.platform.core.CustomizationType;
import com.netsuite.webservices.v2016_2.platform.core.GetCustomizationIdResult;
import com.netsuite.webservices.v2016_2.platform.core.Record;
import com.netsuite.webservices.v2016_2.platform.core.types.GetCustomizationType;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.platform.messages.GetCustomizationIdRequest;
import com.netsuite.webservices.v2016_2.platform.messages.GetListRequest;
import com.netsuite.webservices.v2016_2.setup.customization.CustomRecordType;

/**
 *
 */
public class DefaultCustomMetaDataSourceImpl extends DefaultCustomMetaDataSource<NetSuitePortType> {

    public DefaultCustomMetaDataSourceImpl(NetSuiteClientServiceImpl clientService) {
        super(clientService);
    }

    protected List<NsRef> retrieveCustomizationIds(final BasicRecordType type) throws NetSuiteException {
        GetCustomizationIdResult result = clientService.execute(new NetSuiteClientService.PortOperation<GetCustomizationIdResult, NetSuitePortType>() {
            @Override public GetCustomizationIdResult execute(NetSuitePortType port) throws Exception {
                logger.debug("Retrieving customization IDs: {}", type.getType());
                StopWatch stopWatch = new StopWatch();
                try {
                    stopWatch.start();
                    final GetCustomizationIdRequest request = new GetCustomizationIdRequest();
                    CustomizationType customizationType = new CustomizationType();
                    customizationType.setGetCustomizationType(GetCustomizationType.fromValue(type.getType()));
                    request.setCustomizationType(customizationType);
                    return port.getCustomizationId(request).getGetCustomizationIdResult();
                } finally {
                    stopWatch.stop();
                    logger.debug("Retrieved customization IDs: {}, {}", type.getType(), stopWatch);
                }
            }
        });
        if (result.getStatus().getIsSuccess()) {
            List<NsRef> nsRefs;
            if (result.getTotalRecords() > 0) {
                final List<CustomizationRef> refs = result.getCustomizationRefList().getCustomizationRef();
                nsRefs = new ArrayList<>(refs.size());
                for (final CustomizationRef ref : refs) {
                    NsRef nsRef = new NsRef();
                    nsRef.setRefType(RefType.CUSTOMIZATION_REF);
                    nsRef.setScriptId(ref.getScriptId());
                    nsRef.setInternalId(ref.getInternalId());
                    nsRef.setType(ref.getType().value());
                    nsRef.setName(ref.getName());
                    nsRefs.add(nsRef);
                }
            } else {
                nsRefs = Collections.emptyList();
            }
            return nsRefs;
        } else {
            throw new NetSuiteException("Retrieving of customizations was not successful: " + type);
        }
    }

    protected <T> List<T> retrieveCustomizations(final List<NsRef> nsCustomizationRefs) throws NetSuiteException {
        if (nsCustomizationRefs.isEmpty()) {
            return Collections.emptyList();
        }

        final List<CustomizationRef> customizationRefs = new ArrayList<>(nsCustomizationRefs.size());
        for (NsRef nsCustomizationRef : nsCustomizationRefs) {
            CustomizationRef customizationRef = new CustomizationRef();
            customizationRef.setType(RecordType.fromValue(nsCustomizationRef.getType()));
            customizationRef.setScriptId(nsCustomizationRef.getScriptId());
            customizationRef.setInternalId(nsCustomizationRef.getInternalId());
            customizationRefs.add(customizationRef);
        }

        List<NsReadResponse<Record>> result = clientService.execute(new NetSuiteClientService.PortOperation<List<NsReadResponse<Record>>, NetSuitePortType>() {
            @Override public List<NsReadResponse<Record>> execute(NetSuitePortType port) throws Exception {
                logger.debug("Retrieving customizations: {}", nsCustomizationRefs.size());
                StopWatch stopWatch = new StopWatch();
                try {
                    stopWatch.start();
                    final GetListRequest request = new GetListRequest();
                    request.getBaseRef().addAll(customizationRefs);
                    return toNsReadResponseList(port.getList(request).getReadResponseList());
                } finally {
                    stopWatch.stop();
                    logger.debug("Retrieved customizations: {}, {}", nsCustomizationRefs.size(), stopWatch);
                }
            }
        });
        if (!result.isEmpty()) {
            List<T> customizations = new ArrayList<>(result.size());
            for (NsReadResponse response : result) {
                if (response.getStatus().isSuccess()) {
                    customizations.add((T) response.getRecord());
                } else {
                    throw new NetSuiteException("Retrieving of customization was not successful: " + response.getStatus());
                }
            }
            return customizations;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected Map<String, CustomFieldDesc> retrieveCustomRecordCustomFields(
            RecordTypeDesc recordType, NsRef nsCustomizationRef) throws NetSuiteException {

        List<CustomRecordType> customizationList = retrieveCustomizations(Collections.singletonList(nsCustomizationRef));

        if (customizationList.isEmpty()) {
            return null;
        }

        CustomRecordType customRecordType = customizationList.get(0);

        List<?> customFieldList = customRecordType.getCustomFieldList().getCustomField();

        Map<String, CustomFieldDesc> customFieldDescMap = createCustomFieldDescMap(recordType,
                BasicRecordType.getByType(nsCustomizationRef.getType()), customFieldList);

        return customFieldDescMap;
    }

}
