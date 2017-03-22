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

import static org.talend.components.netsuite.client.model.beans.Beans.getEnumFromStringMapper;
import static org.talend.components.netsuite.client.model.beans.Beans.getEnumToStringMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.talend.components.netsuite.util.Mapper;

/**
 *
 */
public class SearchFieldOperatorTypeDesc<T> {
    private SearchFieldOperatorType operatorType;
    private Class<T> operatorClass;
    private Mapper<T, String> mapper;
    private Mapper<String, T> reverseMapper;

    public SearchFieldOperatorTypeDesc(SearchFieldOperatorType operatorType, Class<T> operatorClass,
            Mapper<T, String> mapper, Mapper<String, T> reverseMapper) {

        this.operatorType = operatorType;
        this.operatorClass = operatorClass;
        this.mapper = mapper;
        this.reverseMapper = reverseMapper;
    }

    public SearchFieldOperatorType getOperatorType() {
        return operatorType;
    }

    public String getOperatorTypeName() {
        return operatorType.getOperatorTypeName();
    }

    public Class<T> getOperatorClass() {
        return operatorClass;
    }

    public Mapper<T, String> getMapper() {
        return mapper;
    }

    public Mapper<String, T> getReverseMapper() {
        return reverseMapper;
    }

    public String mapToString(T stringValue) {
        return mapper.map(stringValue);
    }

    public Object mapFromString(String stringValue) {
        return reverseMapper.map(stringValue);
    }

    public SearchFieldOperatorName getOperatorName(Object value) {
        if (operatorType == SearchFieldOperatorType.BOOLEAN) {
            return SearchFieldOperatorType.SearchBooleanFieldOperator.NAME;
        } else {
            return new SearchFieldOperatorName(operatorType.getDataType(), mapToString((T) value));
        }
    }

    public Object getOperator(String qualifiedName) {
        SearchFieldOperatorName opName = new SearchFieldOperatorName(qualifiedName);
        if (operatorType == SearchFieldOperatorType.BOOLEAN) {
            if (!opName.equals(SearchFieldOperatorType.SearchBooleanFieldOperator.NAME)) {
                throw new IllegalArgumentException(
                        "Invalid operator type: " + "'" + qualifiedName + "' != '" + opName.getDataType() + "'");
            }
            return SearchFieldOperatorType.SearchBooleanFieldOperator.INSTANCE;
        } else {
            if (!opName.getDataType().equals(operatorType.getDataType())) {
                throw new IllegalArgumentException(
                        "Invalid operator data type: " + "'" + opName.getDataType() + "' != '" + operatorType.getDataType() + "'");
            }
            return mapFromString(opName.getName());
        }
    }

    public boolean hasOperator(SearchFieldOperatorName operatorName) {
        return operatorType.getDataType().equals(operatorName.getDataType());
    }

    public List<SearchFieldOperatorName> getOperatorNames() {
        if (operatorClass.isEnum()) {
            Enum[] values = ((Class<? extends Enum>) getOperatorClass()).getEnumConstants();
            List<SearchFieldOperatorName> names = new ArrayList<>(values.length);
            for (Enum value : values) {
                names.add(getOperatorName(value));
            }
            return names;
        } else if (operatorClass == SearchFieldOperatorType.SearchBooleanFieldOperator.class) {
            return Arrays.asList(SearchFieldOperatorType.SearchBooleanFieldOperator.NAME);
        } else {
            throw new IllegalStateException("Unsupported operator type: " + operatorClass);
        }
    }

    public List<?> getOperators() {
        if (operatorClass.isEnum()) {
            Enum[] values = ((Class<? extends Enum>) getOperatorClass()).getEnumConstants();
            return Arrays.asList(values);
        } else if (operatorType == SearchFieldOperatorType.BOOLEAN) {
            return Arrays.asList(SearchFieldOperatorType.SearchBooleanFieldOperator.INSTANCE);
        } else {
            throw new IllegalStateException("Unsupported operator type: " + operatorType);
        }
    }

    public static SearchFieldOperatorTypeDesc<SearchFieldOperatorType.SearchBooleanFieldOperator> createForBoolean() {
        return new SearchFieldOperatorTypeDesc<>(SearchFieldOperatorType.BOOLEAN,
                SearchFieldOperatorType.SearchBooleanFieldOperator.class, null, null);
    }

    public static <T> SearchFieldOperatorTypeDesc<T> createForEnum(SearchFieldOperatorType operatorType, Class<T> clazz) {
        return new SearchFieldOperatorTypeDesc<>(operatorType, clazz,
                (Mapper<T, String>) getEnumToStringMapper((Class<Enum>) clazz),
                (Mapper<String, T>) getEnumFromStringMapper((Class<Enum>) clazz));
    }

}
