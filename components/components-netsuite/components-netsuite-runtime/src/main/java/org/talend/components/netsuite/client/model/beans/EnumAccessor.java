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

package org.talend.components.netsuite.client.model.beans;

/**
 * Used to access enum values for enum classes generated from NetSuite's XML schemas.
 */
public interface EnumAccessor {

    /**
     * Get string value for given enum constant.
     *
     * @param enumValue enum value
     * @return string value
     */
    String getStringValue(Enum enumValue);

    /**
     * Get enum constant for given string value.
     *
     * @param value string value
     * @return enum constant
     */
    Enum getEnumValue(String value);
}
