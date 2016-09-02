// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship.common;

import java.util.Arrays;
import java.util.List;

public class TdsConstants {

    public static final String API_VERSION = "api/v1"; //$NON-NLS-1$

    public static final String WIDGET_TYPE_CAMPAIGN_CHOOSER = "widget.type.campaign.chooser"; //$NON-NLS-1$

    public static final String META_PREFIX = "TDS_"; //$NON-NLS-1$

    public static final String META_ID = "TDS_ID"; //$NON-NLS-1$

    public static final String META_STATE = "TDS_STATE"; //$NON-NLS-1$

    public static final String META_ASSIGNEE = "TDS_ASSIGNEE"; //$NON-NLS-1$

    public static final String META_LAST_UPDATE = "TDS_LAST_UPDATE"; //$NON-NLS-1$

    public static final String META_LAST_UPDATED_BY = "TDS_LAST_UPDATED_BY"; //$NON-NLS-1$

    public static final String META_PRIORITY = "TDS_PRIORITY"; //$NON-NLS-1$

    public static final String META_TAGS = "TDS_TAGS"; //$NON-NLS-1$

    public static final String META_GID = "TDS_GID"; //$NON-NLS-1$

    public static final String META_MASTER = "TDS_MASTER"; //$NON-NLS-1$

    public static final String META_SOURCE = "TDS_SOURCE"; //$NON-NLS-1$

    public static final String META_SCORE = "TDS_SCORE"; //$NON-NLS-1$

    public static final String PRIORITY_VERY_LOW = "Very Low"; //$NON-NLS-1$

    public static final String PRIORITY_LOW = "Low"; //$NON-NLS-1$

    public static final String PRIORITY_MEDIUM = "Medium"; //$NON-NLS-1$

    public static final String PRIORITY_HIGH = "High"; //$NON-NLS-1$

    public static final String PRIORITY_VERY_HIGH = "Very High"; //$NON-NLS-1$

    public static final List<String> PRIORITY_LIST = Arrays.asList(PRIORITY_VERY_LOW, PRIORITY_LOW, PRIORITY_MEDIUM,
            PRIORITY_HIGH, PRIORITY_VERY_HIGH);

}
