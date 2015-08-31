// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.base;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

@RestController @Api(value = "components", basePath = "/components", description = "Component services") @Service public class ComponentService {

    protected Map<Integer, ComponentProperties> propertiesMap = new HashMap<Integer, ComponentProperties>();

    @Autowired private ApplicationContext context;

    public ComponentService() {
    }

    /**
     * Used to get a new {@link ComponentProperties} object for the specified component.
     * <p>
     * The {@code ComponentProperties} has everything required to render a UI and as well
     * capture and validate the values of the properties associated with the component, based
     * on interactions with this service.
     *
     * @param name the name of the component
     * @return a {@code ComponentProperties} object.
     */
    @RequestMapping(value = "/components/{name}/properties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) public @ResponseBody ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "name of the components") String name) {
        final String beanName = Constants.COMPONENT_BEAN_PREFIX + name;
        Object beans = context.getBeansOfType(Object.class);
        final ComponentDefinition compDef = context.getBean(beanName, ComponentDefinition.class);
        ComponentProperties properties = compDef.createProperties();
        return properties;
    }

    @RequestMapping(value = "/components/validateProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE) public @ResponseBody ComponentProperties validateProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties) {
        properties.validateProperty(propName);
        return properties;
    }

}
