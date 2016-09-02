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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.talend.components.datastewardship.common.CampaignDetail.RecordField;

public class TdsUtils {

    /**
     * Return api/v1/campaigns/owned
     */
    public static String getCampaignResource() {
        return TdsConstants.API_VERSION + "/campaigns/owned"; //$NON-NLS-1$
    }

    /**
     * Return api/v1/campaigns/owned/{campaignName}
     */
    public static String getCampaignDetailResource(String campaignName) {
        return TdsConstants.API_VERSION + "/campaigns/owned/" + campaignName; //$NON-NLS-1$
    }

    /**
     * Return api/v1/campaigns/owned/{campaignName}/tasks
     */
    public static String getTaskResource(String campaignName) {
        return getCampaignDetailResource(campaignName) + "/tasks"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Return api/v1/campaigns/owned/{campaignName}/tasks/{taskState}
     */
    public static String getTaskQueryResource(String campaignName, String taskState) {
        return getTaskResource(campaignName) + "/" + taskState; //$NON-NLS-1$
    }

    /**
     * Check if the field is TDS task meta data
     */
    public static boolean isMetadataField(String nameInSchema) {
        return nameInSchema.startsWith(TdsConstants.META_PREFIX);
    }

    /**
     * Check if the field is TDS_MASTER
     */
    public static boolean isMasterField(String nameInSchema) {
        return nameInSchema.equals(TdsConstants.META_MASTER);
    }

    /**
     * Check if the field is TDS_SOURCE
     */
    public static boolean isSourceField(String nameInSchema) {
        return nameInSchema.equals(TdsConstants.META_SOURCE);
    }

    /**
     * Return real field name in task's raw JSON
     */
    public static String getNameInJson(String nameInSchema) {
        if (nameInSchema.equals(TdsConstants.META_ID)) {
            return "id"; //$NON-NLS-1$
        } else if (nameInSchema.equals(TdsConstants.META_STATE)) {
            return "currentState"; //$NON-NLS-1$
        } else if (nameInSchema.equals(TdsConstants.META_ASSIGNEE)) {
            return "assignee"; //$NON-NLS-1$
        } else if (nameInSchema.equals(TdsConstants.META_LAST_UPDATE)) {
            return "lastUpdateDate"; //$NON-NLS-1$
        } else if (nameInSchema.equals(TdsConstants.META_LAST_UPDATED_BY)) {
            return "lastUpdatedBy"; //$NON-NLS-1$
        } else if (nameInSchema.equals(TdsConstants.META_TAGS)) {
            return null;// Not supported yet
        } else {
            return null;
        }
    }

    public static String formatDate(long value) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
        return df.format(new Date(value));
    }

    public static String addQuotes(String str) {
        String newStr = str;
        if (str == null) {
            return ""; //$NON-NLS-1$
        }
        if (newStr.contains("\"")) { //$NON-NLS-1$
            newStr = newStr.replace("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return "\"" + newStr + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static List<RecordField> getMetadataFieldsForOutputMerging() {
        List<RecordField> fields = new ArrayList<RecordField>();
        fields.add(new RecordField(TdsConstants.META_GID, "text", true, false)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_MASTER, "boolean", true, false)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_SOURCE, "text", false, false)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_SCORE, "double", false, false)); //$NON-NLS-1$
        return fields;
    }

    public static List<RecordField> getMetadataFieldsForInput(boolean isMerging, boolean goldenOnly) {
        List<RecordField> fields = new ArrayList<RecordField>();
        fields.add(new RecordField(TdsConstants.META_ID, "text", true, true)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_STATE, "text", true, true)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_ASSIGNEE, "text", false, true)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_LAST_UPDATE, "long", true, true)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_LAST_UPDATED_BY, "text", false, true)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_PRIORITY, "integer", false, true)); //$NON-NLS-1$
        fields.add(new RecordField(TdsConstants.META_TAGS, "text", false, true)); //$NON-NLS-1$
        if (isMerging && !goldenOnly) {
            fields.add(new RecordField(TdsConstants.META_MASTER, "boolean", true, true)); //$NON-NLS-1$
            fields.add(new RecordField(TdsConstants.META_SOURCE, "text", true, true)); //$NON-NLS-1$
        }
        return fields;
    }

    public static int getTaskPriority(String str) {
        if (TdsConstants.PRIORITY_VERY_LOW.equalsIgnoreCase(str)) {
            return 0;
        } else if (TdsConstants.PRIORITY_LOW.equalsIgnoreCase(str)) {
            return 1;
        } else if (TdsConstants.PRIORITY_MEDIUM.equalsIgnoreCase(str)) {
            return 2;
        } else if (TdsConstants.PRIORITY_HIGH.equalsIgnoreCase(str)) {
            return 3;
        } else if (TdsConstants.PRIORITY_VERY_HIGH.equalsIgnoreCase(str)) {
            return 4;
        } else {
            return 0;
        }
    }

}
