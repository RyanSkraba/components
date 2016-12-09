//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DatasetConnectionInfo {

    @JsonProperty("datastore-properties")
    private ObjectNode dataStoreFormData;

    @JsonProperty("dataset-properties")
    private ObjectNode dataSetFormData;

    public ObjectNode getDataStoreFormData() {
        return dataStoreFormData;
    }

    public void setDataStoreFormData(ObjectNode dataStoreFormData) {
        this.dataStoreFormData = dataStoreFormData;
    }

    public ObjectNode getDataSetFormData() {
        return dataSetFormData;
    }

    public void setDataSetFormData(ObjectNode dataSetFormData) {
        this.dataSetFormData = dataSetFormData;
    }
}
