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

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.talend.components.api.Constants;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

import aQute.bnd.annotation.component.Activate;

@RestController
@Api(value = "components", basePath = "/components", description = "Component services")
@Service
public class ComponentServiceImpl implements ComponentService {

    private static final Logger                 LOGGER        = LoggerFactory.getLogger(ComponentServiceImpl.class);

    protected Map<Integer, ComponentProperties> propertiesMap = new HashMap<Integer, ComponentProperties>();

    @Autowired
    private ComponentRegistry                   componentRegistry;

    public ComponentServiceImpl() {
    }

    @Activate
    void activate(BundleContext bundleContext) throws InvalidSyntaxException {
        // this code get called from an OSGI container.
        ServiceReference<ComponentRegistry> serviceReferences = bundleContext.getServiceReference(ComponentRegistry.class);
        if (serviceReferences != null) {
            componentRegistry = bundleContext.getService(serviceReferences);
        } else {// we are in an OSGI world but the service could not be found so issue an error.
            LOGGER.error("failed to find the service :" + ComponentRegistry.class.getCanonicalName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.internal.IComponentService#getComponentProperties(java.lang.String)
     */
    @RequestMapping(value = "/components/{name}/properties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "name of the components") String name) {
        final String beanName = Constants.COMPONENT_BEAN_PREFIX + name;
        ComponentDefinition compDef = componentRegistry.getComponents().get(beanName);
        ComponentProperties properties = compDef.createProperties();
        return properties;
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
        properties.validateProperty(propName);
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
        properties.afterProperty(propName);
        return properties;
    }

    @RequestMapping(value = "/components/beforeProperty/{propName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE) public @ResponseBody ComponentProperties beforeProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties) throws Throwable {
        properties.beforeProperty(propName);
        return properties;
    }    
    
}
