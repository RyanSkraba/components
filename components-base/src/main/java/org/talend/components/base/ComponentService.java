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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

@RestController @Api(value = "components", basePath = "/components", description = "Component services") @Service public class ComponentService {

    protected Map<Integer, ComponentProperties> propertiesMap = new HashMap<Integer, ComponentProperties>();

    @Autowired private ApplicationContext context;

    public ComponentService() {
    }

    @RequestMapping(value = "/components/{name}/properties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) public ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "name of the components") String name) {
        final String beanName = Constants.COMPONENT_BEAN_PREFIX + name;
        final ComponentDefinition compDef = context.getBean(beanName, ComponentDefinition.class);
        ComponentProperties properties = compDef.createProperties();
        return properties;
    }

    @RequestMapping(value = "/components/{id}/validateProperty/{propName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) public ComponentProperties validateProperty(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of ComponentProperties") int id,
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "value", value = "Value of property") String value) {
        ComponentProperties props = propertiesMap.get(id);
        if (props == null) {
            throw new RuntimeException("Not found");
        }
        // How to we communicate the propery is invalid, need to mark the layoutMap object somehowh
        return props;
    }

}
