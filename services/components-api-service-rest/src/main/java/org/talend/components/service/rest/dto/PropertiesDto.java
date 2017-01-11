//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static java.util.Collections.emptyList;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PropertiesDto {

    @JsonProperty("dependencies")
    private List<ObjectNode> dependencies = emptyList();

    @JsonProperty("properties")
    private ObjectNode properties;

    public List<ObjectNode> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ObjectNode> dependencies) {
        this.dependencies = dependencies;
    }

    public ObjectNode getProperties() {
        return properties;
    }

    public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }
}
