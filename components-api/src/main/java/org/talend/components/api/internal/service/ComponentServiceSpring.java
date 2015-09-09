// ============================================================================
//
// copyright (c) 2006-2015 talend inc. - www.talend.com
//
// this source code is available under agreement available at
// %installdir%\features\org.talend.rcp.branding.%productname%\%productname%license.txt
//
// you should have received a copy of the agreement
// along with this program; if not, write to talend sa
// 9 rue pages 92150 suresnes, france
//
// ============================================================================
package org.talend.components.api.internal.service;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.talend.components.api.ComponentDefinition;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.ComponentService;
import org.talend.components.api.ComponentWizardDefinition;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * This is a spring only class that is instanciated by the spring framework. It delegates all its calls to the
 * ComponentServiceImpl delegate create in it's constructor. This delegate uses a Component regitry implementation
 * specific to spring.
 */
@RestController
@Api(value = "components", basePath = ComponentServiceSpring.BASE_PATH, description = "Component services")
@Service
public class ComponentServiceSpring implements ComponentService {

    static final String      BASE_PATH = "/components";

    private ComponentService componentServiceDelegate;

    @Autowired
    public ComponentServiceSpring(final ApplicationContext context) {
        this.componentServiceDelegate = new ComponentServiceImpl(new ComponentRegistry() {

            @Override
            public Map<String, ComponentDefinition> getComponents() {
                return context.getBeansOfType(ComponentDefinition.class);
            }

            @Override
            public Map<String, ComponentWizardDefinition> getComponentWizards() {
                return context.getBeansOfType(ComponentWizardDefinition.class);
            }
        });
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/{name}/properties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "name of the components") String name) {
        return componentServiceDelegate.getComponentProperties(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/validateProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties validateProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.validateProperty(propName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/beforeProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties beforeProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.beforeProperty(propName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/afterProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties afterProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.afterProperty(propName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<String> getAllComponentNames() {
        return componentServiceDelegate.getAllComponentNames();
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/definition", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<ComponentDefinition> getAllComponents() {
        return componentServiceDelegate.getAllComponents();
    }

    @RequestMapping(value = BASE_PATH + "/allTopLevelWizards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return componentServiceDelegate.getTopLevelComponentWizards();
    }

}
