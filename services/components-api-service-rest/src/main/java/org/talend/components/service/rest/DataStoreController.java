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

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.annotation.Service;

/**
 * This is needed because when annotating the interface with Spring MVC, the ones on methods parameters were not taken in account.
 */
@Service(name = "DataStoreController")
public interface DataStoreController {

    /**
     * Return all known DataStore definitions.
     *
     * @return all known DataStore definitions.
     * @returnWrapped java.lang.Iterable<org.talend.components.common.datastore.DatastoreDefinition>
     */
    @RequestMapping(value = "/datastoresDefinitions", method = GET)
    Iterable<DataStoreDefinitionDTO> listDataStoreDefinitions();

    /**
     * Return the wanted DataStore definition.
     *
     * @param dataStoreName the name of the wanted datastore.
     * @return the wanted DataStore definition.
     * @returnWrapped org.talend.components.common.datastore.DatastoreDefinition
     */
    @RequestMapping(value = "/datastoresDefinitions/{dataStoreName}/properties", method = GET)
    String getDatastoreProperties(@PathVariable(value = "dataStoreName") String dataStoreName);

    /**
     * Validates the given datastore definitions.
     *
     * @param dataStoreName the name of the datastore to validate.
     * @param properties the datastore properties to validate.
     * @HTTP 204 If the given properties is valid.
     * @HTTP 400 If the given properties is not valid.
     */
    @RequestMapping(value = "/datastoresDefinitions/{dataStoreName}/test", method = POST)
    void validateDatastoreDefinition(@PathVariable(value = "dataStoreName") String dataStoreName,
            @RequestBody DatastoreProperties properties);

    /**
     * Validates the given datastore definitions.
     *
     * @param dataStoreName the name of the datastore to validate.
     * @param dataStoreProperties the datastore properties to validate.
     * @HTTP 204 If the given definition is valid.
     * @HTTP 400 If the given definition is not valid.
     * @HTTP 404 If the data store is not found
     */
    @RequestMapping(value = "/datastoresDefinitions/{dataStoreName}/testLive", method = POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    boolean checkDatastoreConnection(@PathVariable("dataStoreName") String dataStoreName,
            @RequestBody DatastoreProperties dataStoreProperties);

    /**
     * @param propertyName
     * @return
     */
    @RequestMapping(value = "/datastoresDefinitions/{dataStoreName}/properties/{propertyName}/test", method = POST)
    boolean checkDatastoreProperty(@PathVariable("dataStoreName") String dataStoreName, //
            @PathVariable("propertyName") String propertyName, //
            @RequestBody Object value);

}
