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
package org.talend.components.api.internal;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talend.components.api.ComponentDefinition;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.ComponentService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * This is a spring only class that is instanciated by the spring framework. It delegates all its calls to the
 * ComponentServiceImpl delegate create in it's constructor. This delegate uses a Component regitry implementation
 * specific to spring.
 *
 */
@RestController
@Api(value = "components", basePath = "/components", description = "Component services")
@Service
public class ComponentServiceSpring implements ComponentService {

    private ComponentService componentServiceDelegate;

    @Autowired
    public ComponentServiceSpring(final ApplicationContext context) {
        this.componentServiceDelegate = new ComponentServiceImpl(new ComponentRegistry() {

            public Map<String, ComponentDefinition> getComponents() {
                return context.getBeansOfType(ComponentDefinition.class);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.internal.IComponentService#getComponentProperties(java.lang.String)
     */
    @RequestMapping(value = "/components/{name}/properties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "name of the components") String name) {
        return componentServiceDelegate.getComponentProperties(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.internal.IComponentService#validateProperty(java.lang.String,
     * org.talend.components.api.ComponentProperties)
     */
    @RequestMapping(value = "/components/validateProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties validateProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
                    throws Throwable {
        componentServiceDelegate.validateProperty(propName, properties);
        return properties;
    }

    @RequestMapping(value = "/components/beforeProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties beforeProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
                    throws Throwable {
        componentServiceDelegate.beforeProperty(propName, properties);
        return properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.internal.IComponentService#afterProperty(java.lang.String,
     * org.talend.components.api.ComponentProperties)
     */
    @RequestMapping(value = "/components/afterProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties afterProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
                    throws Throwable {
        componentServiceDelegate.afterProperty(propName, properties);
        return properties;
    }
}
