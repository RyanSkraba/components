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
 * Responsible for handling of search fields and populating of search field with data.
 */
public abstract class SearchFieldAdapter<T> {

    /** Used to get meta data of NetSuite data model. */
    protected BasicMetaData metaData;

    /** Type of search field which this adapter responsible for. */
    protected SearchFieldType fieldType;

    /** Class of search field data object type. */
    protected Class<T> fieldClass;

    public SearchFieldAdapter(BasicMetaData metaData, SearchFieldType fieldType, Class<T> fieldClass) {
        this.metaData = metaData;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
    }

    public SearchFieldType getFieldType() {
        return fieldType;
    }

    /**
     * Populate search field with data.
     *
     * @param operatorName name of search operator to be applied
     * @param values search values to be applied
     * @return search field object
     */
    public T populate(String operatorName, List<String> values) {
        return populate(null, null, operatorName, values);
    }

    /**
     * Populate search field with data.
     *
     * @param internalId internal identifier to be applied to search field
     * @param operatorName name of search operator to be applied
     * @param values search values to be applied
     * @return search field object
     */
    public T populate(String internalId, String operatorName, List<String> values) {
        return populate(null, internalId, operatorName, values);
    }

    /**
     * Populate search field with data.
     *
     * @param fieldObject search field object to populate, can be {@code null}
     * @param internalId internal identifier to be applied to search field
     * @param operatorName name of search operator to be applied
     * @param values search values to be applied
     * @return search field object
     */
    public abstract T populate(T fieldObject, String internalId, String operatorName, List<String> values);

    /**
     * Create instance of NetSuite's search field.
     *
     * @param internalId internal identifier to be applied to a search field
     * @return search field object
     * @throws NetSuiteException if an error occurs during creation of a search field
     */
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
