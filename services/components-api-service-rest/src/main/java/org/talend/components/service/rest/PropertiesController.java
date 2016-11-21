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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.components.service.rest.dto.PropertiesValidationResponse;
import org.talend.components.service.rest.dto.PropertyValidationResponse;
import org.talend.daikon.annotation.Service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Service(name = "PropertiesController")
@RestController
@RequestMapping("properties")
public interface PropertiesController {

    @RequestMapping(value = "{name}", method = GET)
    String getProperties(@PathVariable("name") String definitionName);

    /** Validate the coherence of a set of properties for a specific component. **/
    @RequestMapping(value = "{definitionName}/validate", method = POST)
    ResponseEntity<PropertiesValidationResponse> validateProperties(@PathVariable("definitionName") String definitionName,
                                                                    @RequestBody FormDataContainer formData);

    /** Validate one field. */
    @RequestMapping(value = "{definitionName}/validate/{propName}", method = POST)
    ResponseEntity<PropertyValidationResponse> validateProperty(@PathVariable("definitionName") String definitionName,
                                                                @RequestBody FormDataContainer formData,
                                                                @PathVariable("propName") String propName);

    /** Get dataset properties. Should it be GET? **/
    @RequestMapping(value = "{definitionName}/dataset", method = POST)
    String getDatasetProperties(@PathVariable("definitionName") String definitionName, @RequestBody FormDataContainer formData);

}
