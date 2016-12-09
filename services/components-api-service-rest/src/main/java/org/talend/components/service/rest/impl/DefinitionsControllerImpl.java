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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.components.api.RuntimableDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.service.rest.DefinitionType;
import org.talend.components.service.rest.DefinitionsController;
import org.talend.components.service.rest.dto.ConnectorTypology;
import org.talend.components.service.rest.dto.DefinitionDTO;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import static java.util.stream.StreamSupport.stream;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Definition controller..
 */
@ServiceImplementation
public class DefinitionsControllerImpl implements DefinitionsController {

    /** This class' logger. */
    private static final Logger logger = getLogger(DefinitionsControllerImpl.class);

    @Autowired
    private DefinitionRegistryService definitionServiceDelegate;

    /**
     * Return all known definitions that match the given type.
     *
     * @param type the wanted definition type.
     * @return all known definitions that match the given type.
     * @returnWrapped java.lang.Iterable<org.talend.components.service.rest.dto.DefinitionDTO>
     */
    @Override
    public List<DefinitionDTO> listDefinitions(DefinitionType type) {
        logger.debug("listing definitions for {} ", type);

        Iterable<? extends RuntimableDefinition> definitionsByType = //
                definitionServiceDelegate.getDefinitionsMapByType(type.getTargetClass()).values();

        return stream(definitionsByType.spliterator(), false)
                // this if...else is ugly, one should try to find a better solution
                .map(c -> {
                    if (type == DefinitionType.COMPONENT) {
                        return new DefinitionDTO((ComponentDefinition)c);
                    }
                    else {
                        return new DefinitionDTO((DatastoreDefinition)c);
                    }
                }) //
                .collect(Collectors.toList());
    }

    /**
     * Return components that match the given typology.
     *
     * @param typology the wanted typology.
     * @return the list of all definitions that match the wanted typology.
     * @returnWrapped java.lang.Iterable<org.talend.components.service.rest.dto.DefinitionDTO>
     */
    @Override
    public List<DefinitionDTO> listComponentDefinitions(ConnectorTypology typology) {
        final Collection<ComponentDefinition> definitions = //
                definitionServiceDelegate.getDefinitionsMapByType(ComponentDefinition.class).values();

        Stream<ComponentDefinition> stream = definitions.stream();

        if (typology != null) {
            stream = stream.filter(c -> c.getSupportedConnectorTopologies().contains(typology.getTopology()));
        }

        final List<DefinitionDTO> result = stream //
                .map(DefinitionDTO::new) //
                .collect(Collectors.toList());

        logger.debug("found {} component definitions for typology {}", result.size(), typology);

        return result;
    }


}
