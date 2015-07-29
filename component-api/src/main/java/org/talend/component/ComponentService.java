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

@RestController
@Api(value = "components", basePath = "/components", description = "Component services")
@Service
public class ComponentService {

	/**
	 * Injected, this is temporary only for testing, we need to have a means of binding the component name
	 * with multiple instances of ComponentDesign.
	 */
	protected ComponentDesign design;
	
	protected int nextId;
	
	protected Map<Integer, ComponentProperties> propertiesMap = new HashMap<Integer, ComponentProperties>();
	
	@Autowired
	public ComponentService(ComponentDesign design) {
		System.out.println("design: " + design);
		this.design = design;
	}
	
	@RequestMapping(value = "/components/{name}/newProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ComponentProperties newComponentProperties(
			@PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the component") String componentName) {
		ComponentProperties cp =  getDesign().createProperties();
		cp.setId(++nextId);
		propertiesMap.put(cp.getId(), cp);
		return cp;
	}

	@RequestMapping(value = "/components/{id}/existingProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ComponentProperties existingComponentProperties(
			@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of ComponentProperties") int id) {
		return propertiesMap.get(id);
	}

	public ComponentDesign getDesign() {
		return design;
	}

	public void setDesign(ComponentDesign design) {
		this.design = design;
	}

}
