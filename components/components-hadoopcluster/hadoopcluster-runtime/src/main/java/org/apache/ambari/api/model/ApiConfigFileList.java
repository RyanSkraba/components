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
package org.apache.ambari.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is for new way to get configurations, when the server support service_config_versions
 * Then call get method on /configurations/service_config_versions with service_name parameter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiConfigFileList extends ApiListBase<ApiConfigFile> {

    public ApiConfigFileList() {

    }

    public ApiConfigFileList(List<ApiConfigFile> files) {
        super(files);
    }

    private String href;

    @XmlElement
    public String getHref() {
        return href;
    }

    @XmlElement(name = "configurations")
    public List<ApiConfigFile> getFiles() {
        return values;
    }

}
