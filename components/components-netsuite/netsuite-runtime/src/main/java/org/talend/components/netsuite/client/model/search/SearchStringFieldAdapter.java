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

import org.talend.components.netsuite.client.model.BasicMetaData;

/**
 *
 */
public class SearchStringFieldAdapter<T> extends SearchFieldAdapter<T> {

    public SearchStringFieldAdapter(BasicMetaData metaData, SearchFieldType fieldType, Class<T> fieldClass) {
        super(metaData, fieldType, fieldClass);
    }

    @Override
    public T populate(T fieldObject, String internalId, String operatorName, List<String> values) {
        T nsObject = fieldObject != null ? fieldObject : createField(internalId);

        if (values != null && values.size() != 0) {
            setSimpleProperty(nsObject, "searchValue", values.get(0));
            setSimpleProperty(nsObject, "operator",
                    metaData.getSearchFieldOperatorByName(fieldType.getFieldTypeName(), operatorName));
        }

        return nsObject;
    }
}
