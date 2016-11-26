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

package org.talend.components.service.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.talend.components.service.rest.dto.ConnectorTypology;
import org.talend.components.service.rest.dto.DefinitionDTO;
import org.talend.daikon.annotation.Service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Definition controller..
 */
@Service(name = "DefinitionsController")
public interface DefinitionsController {

    /**
     * Return all known definitions that match the given type.
     *
     * @param type the wanted definition type.
     * @return all known definitions that match the given type.
     * @returnWrapped java.lang.Iterable<org.talend.components.service.rest.dto.DefinitionDTO>
     */
    @RequestMapping(value = "/definitions/{type}", method = GET)
    Iterable<DefinitionDTO> listDefinitions(@PathVariable("type") DefinitionType type);

    /**
     * Return components that match the given typology.
     *
     * @param typology the wanted typology.
     * @return the list of all definitions that match the wanted typology.
     * @returnWrapped java.lang.Iterable<org.talend.components.service.rest.dto.DefinitionDTO>
     */
    @RequestMapping(value = "/definitions/components", method = GET)
    Iterable<DefinitionDTO> listComponentDefinitions(@RequestParam(value="typology", required = false) ConnectorTypology typology);


}
