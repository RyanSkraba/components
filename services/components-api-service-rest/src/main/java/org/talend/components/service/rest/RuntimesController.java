/*
 * Copyright (C) 2006-2015 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.components.service.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.talend.daikon.annotation.Service;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Service(name = "RuntimesController")
@RequestMapping(value = "runtimes",
                consumes = APPLICATION_JSON_UTF8_VALUE,
                produces = APPLICATION_JSON_UTF8_VALUE)
public interface RuntimesController {

    /** Validate connection to datastore **/
    // P2
    @RequestMapping(value = "{definitionName}", method = POST)
    void validateConnection(@PathVariable("definitionName") String definitionName);

    /** Validate connection to datastore. Should it be GET method? **/
    // P2
    @RequestMapping(value = "{definitionName}/schema", method = POST)
    void getDatasetSchema(@PathVariable("definitionName") String definitionName);

    /** Get dataset content. Should it be GET method? **/
    // P2
    @RequestMapping(value = "{definitionName}/data", method = POST)
    void getDatasetSchema(@PathVariable("definitionName") String definitionName,
                          @RequestParam(value = "from", defaultValue = "1000") Integer from,
                          @RequestParam(value = "limit", defaultValue = "5000") Integer limit);

}
