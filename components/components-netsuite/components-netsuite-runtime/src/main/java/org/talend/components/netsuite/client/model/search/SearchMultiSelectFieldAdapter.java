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

import static org.talend.components.netsuite.client.model.beans.Beans.getSimpleProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.TypeUtils;

/**
 *
 */
public class SearchMultiSelectFieldAdapter<T> extends SearchFieldAdapter<T> {

    public SearchMultiSelectFieldAdapter(BasicMetaData metaData, SearchFieldType fieldType, Class<T> fieldClass) {
        super(metaData, fieldType, fieldClass);
    }

    @Override
    public T populate(T fieldObject, String internalId, String operatorName, List<String> values) {
        T nsObject = fieldObject != null ? fieldObject : createField(internalId);

        List<Object> searchValue = (List<Object>) getSimpleProperty(nsObject, "searchValue");
        for (int i = 0; i < values.size(); i++) {
            if (StringUtils.isNotEmpty(values.get(i))) {
                Object item = metaData.createInstance("ListOrRecordRef");
                setSimpleProperty(item, "name", values.get(i));
                setSimpleProperty(item, "internalId", values.get(i));
                searchValue.add(item);
            }
        }

        setSimpleProperty(nsObject, "operator", metaData.getSearchFieldOperatorByName(
                fieldType.getFieldTypeName(), operatorName));

        return nsObject;
    }
}
