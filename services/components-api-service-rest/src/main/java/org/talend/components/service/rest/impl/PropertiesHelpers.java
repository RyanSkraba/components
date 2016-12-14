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

package org.talend.components.service.rest.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.components.api.service.ComponentService;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.service.rest.dto.PropertiesWithReferences;
import org.talend.components.service.rest.serialization.JsonSerializationHelper;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ReferenceProperties;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.toInputStream;

@Component
public class PropertiesHelpers {

    @Autowired
    private JsonSerializationHelper jsonSerializationHelper;

    @Autowired
    private DefinitionRegistryService definitionServiceDelegate;

    @Autowired
    private ComponentService componentService;

    public <T extends Properties> T propertiesFromDto(PropertiesWithReferences propertiesContainer) {
        T properties = (T) jsonSerializationHelper.toProperties(
                toInputStream(propertiesContainer.getProperties().toString(), StandardCharsets.UTF_8));
        List<ObjectNode> dependencies = propertiesContainer.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            List<Properties> props = dependencies.stream() //
                    .map(on -> jsonSerializationHelper.toProperties(toInputStream(on.toString(), StandardCharsets.UTF_8))) //
                    .collect(toList());
            props.add(properties);
            ReferenceProperties.resolveReferenceProperties(props, definitionServiceDelegate);
        }
        return properties;
    }

    public Definition<?> getDefinition(String definitionName) {
        return definitionServiceDelegate.getDefinitionsMapByType(Definition.class).get(definitionName);
    }

    public <T extends Definition> T getDefinition(Class<T> clazz, String definitionName) {
        return definitionServiceDelegate.getDefinitionsMapByType(clazz).get(definitionName);
    }

    public DatastoreDefinition getDataStoreDefinition(String definitionName) {
        return getDefinition(DatastoreDefinition.class, definitionName);
    }

    public DatasetDefinition getDataSetDefinition(String definitionName) {
        return getDefinition(DatasetDefinition.class, definitionName);
    }

}
