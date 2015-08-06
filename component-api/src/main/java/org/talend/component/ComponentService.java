package org.talend.component;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

@RestController @Api(value = "components", basePath = "/components", description = "Component services") @Service public class ComponentService {

    /**
     * Injected, this is temporary only for testing, we need to have a means of binding the component name
     * with multiple instances of ComponentDefinition.
     */
    protected ComponentDefinition design;

    protected int nextId;

    protected Map<Integer, ComponentProperties> propertiesMap = new HashMap<Integer, ComponentProperties>();

    /**
     * Temporary for testing a single component which is autowired
     *
     * @param design
     */
    @Autowired public ComponentService(ComponentDefinition design) {
        this.design = design;
    }

    @RequestMapping(value = "/components/{name}/newProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) public ComponentProperties newComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the component") String componentName) {
        ComponentProperties cp = getDesign().createProperties();
        cp.setId(++nextId);
        propertiesMap.put(cp.getId(), cp);
        return cp;
    }

    @RequestMapping(value = "/components/{id}/existingProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) public ComponentProperties existingComponentProperties(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of ComponentProperties") int id) {
        return propertiesMap.get(id);
    }

    @RequestMapping(value = "/components/{id}/validateProperty/{propName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) public ComponentProperties validateProperty(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of ComponentProperties") int id,
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
         @ApiParam(name = "value", value = "Value of property") String value) {
        ComponentProperties props = propertiesMap.get(id);
        if (props == null) {
            throw new RuntimeException("Not found");
        }
        // How to we communicate the propery is invalid, need to mark the properties object somehowh
        return props;
    }

    public ComponentDefinition getDesign() {
        return design;
    }

    public void setDesign(ComponentDefinition design) {
        this.design = design;
    }

}
