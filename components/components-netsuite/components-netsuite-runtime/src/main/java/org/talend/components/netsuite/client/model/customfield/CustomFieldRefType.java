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

package org.talend.components.netsuite.client.model.customfield;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum CustomFieldRefType {
    BOOLEAN("BooleanCustomFieldRef"),
    DOUBLE("DoubleCustomFieldRef"),
    LONG("LongCustomFieldRef"),
    STRING("StringCustomFieldRef"),
    DATE("DateCustomFieldRef"),
    SELECT("SelectCustomFieldRef"),
    MULTI_SELECT("MultiSelectCustomFieldRef");

    private String typeName;

    CustomFieldRefType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    private static final Map<String, CustomFieldRefType> customFieldRefTypeMap = new HashMap<>();

    static {
        customFieldRefTypeMap.put("_checkBox", CustomFieldRefType.BOOLEAN);
        customFieldRefTypeMap.put("_currency", CustomFieldRefType.DOUBLE);
        customFieldRefTypeMap.put("_date", CustomFieldRefType.DATE);
        customFieldRefTypeMap.put("_datetime", CustomFieldRefType.DATE);
        customFieldRefTypeMap.put("_decimalNumber", CustomFieldRefType.DOUBLE);
        customFieldRefTypeMap.put("_document", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_eMailAddress", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_freeFormText", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_help", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_hyperlink", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_image", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_inlineHTML", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_integerNumber", CustomFieldRefType.LONG);
        customFieldRefTypeMap.put("_listRecord", CustomFieldRefType.SELECT);
        customFieldRefTypeMap.put("_longText", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_multipleSelect", CustomFieldRefType.MULTI_SELECT);
        customFieldRefTypeMap.put("_password", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_percent", CustomFieldRefType.DOUBLE);
        customFieldRefTypeMap.put("_phoneNumber", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_richText", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_textArea", CustomFieldRefType.STRING);
        customFieldRefTypeMap.put("_timeOfDay", CustomFieldRefType.DATE);
    }

    public static CustomFieldRefType getByCustomizationType(String customizationType) {
        return customFieldRefTypeMap.get(customizationType);
    }

    public static CustomFieldRefType getByCustomizationTypeOrDefault(String customizationType) {
        CustomFieldRefType customFieldRefType = getByCustomizationType(customizationType);
        if (customFieldRefType == null) {
            customFieldRefType = CustomFieldRefType.STRING;
        }
        return customFieldRefType;
    }
}
