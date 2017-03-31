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

package org.talend.components.netsuite;

import org.talend.daikon.di.DiSchemaConstants;

/**
 * Holds schema related constants that are used by NetSuite classes.
 */
public abstract class NetSuiteSchemaConstants {

    public static final String NS_PREFIX = DiSchemaConstants.TALEND6_ADDITIONAL_PROPERTIES + "netsuite.";

    public static final String NS_CUSTOM_RECORD = NS_PREFIX + "customRecord";
    public static final String NS_CUSTOM_RECORD_SCRIPT_ID = NS_PREFIX + "customRecord.scriptId";
    public static final String NS_CUSTOM_RECORD_INTERNAL_ID = NS_PREFIX + "customRecord.internalId";
    public static final String NS_CUSTOM_RECORD_CUSTOMIZATION_TYPE = NS_PREFIX + "customRecord.customizationType";
    public static final String NS_CUSTOM_RECORD_TYPE = NS_PREFIX + "customRecord.type";

    public static final String NS_CUSTOM_FIELD = NS_PREFIX + "customField";
    public static final String NS_CUSTOM_FIELD_SCRIPT_ID = NS_PREFIX + "customField.scriptId";
    public static final String NS_CUSTOM_FIELD_INTERNAL_ID = NS_PREFIX + "customField.internalId";
    public static final String NS_CUSTOM_FIELD_CUSTOMIZATION_TYPE = NS_PREFIX + "customField.customizationType";
    public static final String NS_CUSTOM_FIELD_TYPE = NS_PREFIX + "customField.type";
}
