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

package org.talend.components.netsuite.client.model;

import static org.talend.components.netsuite.client.model.beans.Beans.toInitialUpper;

import java.util.Arrays;
import java.util.Collection;

import org.talend.components.netsuite.client.model.search.SearchFieldOperatorType;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorTypeDesc;
import org.talend.components.netsuite.test.client.model.TestRecordTypeEnum;
import org.talend.components.netsuite.test.client.model.TestSearchRecordTypeEnum;

import com.netsuite.webservices.test.platform.core.BaseRef;
import com.netsuite.webservices.test.platform.core.CustomFieldList;
import com.netsuite.webservices.test.platform.core.CustomFieldRef;
import com.netsuite.webservices.test.platform.core.ListOrRecordRef;
import com.netsuite.webservices.test.platform.core.NullField;
import com.netsuite.webservices.test.platform.core.SearchBooleanCustomField;
import com.netsuite.webservices.test.platform.core.SearchBooleanField;
import com.netsuite.webservices.test.platform.core.SearchCustomFieldList;
import com.netsuite.webservices.test.platform.core.SearchDateCustomField;
import com.netsuite.webservices.test.platform.core.SearchDateField;
import com.netsuite.webservices.test.platform.core.SearchDoubleCustomField;
import com.netsuite.webservices.test.platform.core.SearchDoubleField;
import com.netsuite.webservices.test.platform.core.SearchEnumMultiSelectCustomField;
import com.netsuite.webservices.test.platform.core.SearchEnumMultiSelectField;
import com.netsuite.webservices.test.platform.core.SearchLongCustomField;
import com.netsuite.webservices.test.platform.core.SearchLongField;
import com.netsuite.webservices.test.platform.core.SearchMultiSelectCustomField;
import com.netsuite.webservices.test.platform.core.SearchMultiSelectField;
import com.netsuite.webservices.test.platform.core.SearchStringCustomField;
import com.netsuite.webservices.test.platform.core.SearchStringField;
import com.netsuite.webservices.test.platform.core.SearchTextNumberField;
import com.netsuite.webservices.test.platform.core.types.SearchDate;
import com.netsuite.webservices.test.platform.core.types.SearchDateFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchDoubleFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchLongFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchStringFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchTextNumberFieldOperator;

/**
 *
 */
public class TestBasicMetaDataImpl extends BasicMetaData {

    private static TestBasicMetaDataImpl instance = new TestBasicMetaDataImpl();

    public static TestBasicMetaDataImpl getInstance() {
        return instance;
    }

    public TestBasicMetaDataImpl() {
        bindTypeHierarchy(BaseRef.class);
        bindTypeHierarchy(CustomFieldRef.class);

        bindType(NullField.class, null);
        bindType(ListOrRecordRef.class, null);
        bindType(CustomFieldList.class, null);
        bindType(SearchCustomFieldList.class, null);

        bindSearchFields(Arrays.asList(
                SearchBooleanCustomField.class,
                SearchBooleanField.class,
                SearchDateCustomField.class,
                SearchDateField.class,
                SearchDoubleCustomField.class,
                SearchDoubleField.class,
                SearchEnumMultiSelectField.class,
                SearchEnumMultiSelectCustomField.class,
                SearchMultiSelectCustomField.class,
                SearchMultiSelectField.class,
                SearchLongCustomField.class,
                SearchLongField.class,
                SearchStringCustomField.class,
                SearchStringField.class,
                SearchTextNumberField.class
        ));

        bindSearchFieldOperatorTypes(Arrays.<SearchFieldOperatorTypeDesc>asList(
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.DATE, SearchDateFieldOperator.class),
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.PREDEFINED_DATE, SearchDate.class),
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.LONG, SearchLongFieldOperator.class),
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.DOUBLE, SearchDoubleFieldOperator.class),
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.STRING, SearchStringFieldOperator.class),
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.TEXT_NUMBER, SearchTextNumberFieldOperator.class),
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.MULTI_SELECT, SearchMultiSelectFieldOperator.class),
                SearchFieldOperatorTypeDesc.createForEnum(SearchFieldOperatorType.ENUM_MULTI_SELECT, SearchEnumMultiSelectFieldOperator.class)
        ));
    }

    @Override
    public Collection<RecordTypeDesc> getRecordTypes() {
        return Arrays.<RecordTypeDesc>asList(TestRecordTypeEnum.values());
    }

    @Override
    public RecordTypeDesc getRecordType(String recordType) {
        return TestRecordTypeEnum.getByTypeName(toInitialUpper(recordType));
    }

    @Override
    public SearchRecordTypeDesc getSearchRecordType(String searchRecordType) {
        return TestSearchRecordTypeEnum.getByTypeName(toInitialUpper(searchRecordType));
    }
}
