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
import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.util.List;

import org.talend.components.netsuite.client.model.BasicMetaData;

/**
 * Search field adapter for {@code SearchEnumMultiSelectField} and {@code SearchEnumMultiSelectCustomField}.
 */
public class SearchEnumMultiSelectFieldAdapter<T> extends SearchFieldAdapter<T> {

    public SearchEnumMultiSelectFieldAdapter(BasicMetaData metaData, SearchFieldType fieldType, Class<T> fieldClass) {
        super(metaData, fieldType, fieldClass);
    }

    @Override
    public T populate(T fieldObject, String internalId, String operatorName, List<String> values) {
        T nsObject = fieldObject != null ? fieldObject : createField(internalId);

        List<String> searchValue = (List<String>) getSimpleProperty(nsObject, "searchValue");
        searchValue.addAll(values);

        setSimpleProperty(nsObject, "operator",
                metaData.getSearchFieldOperatorByName(fieldType.getFieldTypeName(), operatorName));

        return nsObject;
    }
}
