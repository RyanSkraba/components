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
package org.talend.components.marketo.runtime.client.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListOperationParameters {

    String operation;

    Boolean strict = Boolean.FALSE;

    String apiMode;

    // SOAP Parameters
    String listKeyType = "MKTOLISTNAME"; // Possible values: MKTOLISTNAME, MKTOSALESUSERID, SFDCLEADOWNERID.

    String listKeyValue = "";

    String leadKeyType = "IDNUM"; // According API doc => Possible values: IDNUM.

    List<String> leadKeyValue = new ArrayList<String>();

    // REST parameters
    Integer listId = 0;

    List<Integer> leadIds = new ArrayList<Integer>();

    public Boolean isValid() {
        if ("SOAP".equals(apiMode)) {
            return (listKeyType != null && !listKeyType.isEmpty()) && (listKeyValue != null && !listKeyValue.isEmpty())
                    && (leadKeyType != null && !leadKeyType.isEmpty()) && (leadKeyValue != null && !leadKeyValue.isEmpty());
        }
        if ("REST".equals(apiMode)) {
            return (listId != null && leadIds != null && !leadIds.isEmpty());
        }
        return false;
    }

    public void reset() {
        // SOAP
        listKeyType = "";
        listKeyValue = "";
        leadKeyType = "";
        leadKeyValue.clear();
        // REST
        listId = null;
        leadIds.clear();
    }

    public String getApiMode() {
        return apiMode;
    }

    public void setApiMode(String apiMode) {
        this.apiMode = apiMode;
    }

    public String[] getLeadKeyValues() {
        return leadKeyValue.toArray(new String[] {});
    }

    public void setLeadKeyValue(String[] leadKeyValue) {
        if (leadKeyValue == null) {
            this.leadKeyValue = null;
        } else {
            this.leadKeyValue = new ArrayList<>(Arrays.asList(leadKeyValue));
        }
    }

    public String getListKeyType() {
        return listKeyType;
    }

    public void setListKeyType(String listKeyType) {
        this.listKeyType = listKeyType;
    }

    public String getListKeyValue() {
        return listKeyValue;
    }

    public void setListKeyValue(String listKeyValue) {
        this.listKeyValue = listKeyValue;
    }

    public String getLeadKeyType() {
        return leadKeyType;
    }

    public void setLeadKeyType(String leadKeyType) {
        this.leadKeyType = leadKeyType;
    }

    public Integer getListId() {
        return listId;
    }

    public void setListId(Integer listId) {
        this.listId = listId;
    }

    public List<String> getLeadKeyValue() {
        return leadKeyValue;
    }

    public List<Integer> getLeadIds() {
        return leadIds;
    }

    public Integer[] getLeadIdsValues() {
        return leadIds.toArray(new Integer[] {});
    }

    public void setLeadIds(Integer[] leadIds) {
        if (leadIds == null) {
            this.leadIds = null;
        } else {
            this.leadIds = new ArrayList<>(Arrays.asList(leadIds));
        }
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ListOperationParameters{");
        sb.append("operation='").append(operation).append('\'');
        sb.append(", strict=").append(strict);
        sb.append(", isValid=").append(isValid());
        sb.append(", apiMode='").append(apiMode).append('\'');
        sb.append(", listKeyType='").append(listKeyType).append('\'');
        sb.append(", listKeyValue='").append(listKeyValue).append('\'');
        sb.append(", leadKeyType='").append(leadKeyType).append('\'');
        sb.append(", leadKeyValue=").append(leadKeyValue == null ? "null" : leadKeyValue);
        sb.append(", listId=").append(listId);
        sb.append(", leadIds=").append(leadIds == null ? "null" : leadIds);
        sb.append('}');
        return sb.toString();
    }
}
