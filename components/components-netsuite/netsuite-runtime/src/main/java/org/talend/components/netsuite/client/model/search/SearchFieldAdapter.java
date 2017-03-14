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

import static org.talend.components.netsuite.client.model.beans.Beans.setProperty;

import java.util.List;

import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;

/**
 *
 */
public abstract class SearchFieldAdapter<T> {
    protected BasicMetaData metaData;
    protected SearchFieldType fieldType;
    protected Class<T> fieldClass;

    public SearchFieldAdapter(BasicMetaData metaData, SearchFieldType fieldType, Class<T> fieldClass) {
        this.metaData = metaData;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
    }

    public T populate(String operatorName, List<String> values) {
        return populate(null, null, operatorName, values);
    }

    public T populate(String internalId, String operatorName, List<String> values) {
        return populate(null, internalId, operatorName, values);
    }

    public abstract T populate(T fieldObject, String internalId, String operatorName, List<String> values);

    protected T createField(String internalId) throws NetSuiteException {
        try {
            BeanInfo fieldTypeMetaData = Beans.getBeanInfo(fieldClass);
            T searchField = fieldClass.newInstance();
            if (fieldTypeMetaData.getProperty("internalId") != null && internalId != null) {
                setProperty(searchField, "internalId", internalId);
            }
            return searchField;
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
    }

}
