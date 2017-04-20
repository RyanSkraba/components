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

package org.talend.components.netsuite.client.model.search;

import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.netsuite.client.model.BasicMetaData;

/**
 *
 */
public class SearchLongFieldAdapter<T> extends SearchFieldAdapter<T> {

    public SearchLongFieldAdapter(BasicMetaData metaData, SearchFieldType fieldType, Class<T> fieldClass) {
        super(metaData, fieldType, fieldClass);
    }

    @Override
    public T populate(T fieldObject, String internalId, String operatorName, List<String> values) {
        T nsObject = fieldObject != null ? fieldObject : createField(internalId);

        if (values != null && values.size() != 0) {
            setSimpleProperty(nsObject, "searchValue", Long.valueOf(Long.parseLong(values.get(0))));
            if (values.size() > 1 && StringUtils.isNotEmpty(values.get(1))) {
                setSimpleProperty(nsObject, "searchValue2", Long.valueOf(Long.parseLong(values.get(1))));
            }
        }

        setSimpleProperty(nsObject, "operator",
                metaData.getSearchFieldOperatorByName(fieldType.getFieldTypeName(), operatorName));

        return nsObject;
    }
}
