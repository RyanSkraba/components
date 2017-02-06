// ==============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ==============================================================================

package org.talend.components.service.rest.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.Definition;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO used for the definitions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefinitionDTO {

    /** Unique name to identify the data store. */
    private String name;

    /** Presentation name for UI. */
    private String label;

    /** Icon representing the DS. */
    private String iconURL;

    /** Types supported by the DS. */
    private List<String> types;

    /** The input component name. **/
    private String inputCompName;

    /** The output component name. **/
    private String outputCompName;

    /** The definition type. */
    private String type;

    /** The supported typologies. */
    private Set<String> typologies = null;

    /** The supported execution engines. */
    private Set<String> engines = null;

    /**
     * Default empty constructor.
     */
    public DefinitionDTO() {
        // empty constructor needed for json serialization
    }

    /**
     * Constructor for the RuntimableDefinition part.
     *
     * @param origin the RuntimableDefinition.
     */
    private DefinitionDTO(Definition origin) {
        this.name = origin.getName();
        this.label = origin.getDisplayName();
        this.iconURL = buildImageUrl(origin.getName()); // TODO hmdebenque why not use origin.getImagePath() ?
    }

    /**
     * Create a DefinitionDTO out of the given DataStoreDefinition.
     *
     * @param origin the datastore definition.
     */
    public DefinitionDTO(DatastoreDefinition origin) {
        this((Definition) origin);
        this.type = "datastore";
        this.inputCompName = origin.getInputCompDefinitionName();
        this.outputCompName = origin.getOutputCompDefinitionName();
    }

    /**
     * Create a DefinitionDTO out of the given ComponentDefinition.
     *
     * @param origin the component definition.
     */
    public DefinitionDTO(ComponentDefinition origin) {
        this((Definition) origin);
        this.type = "component";
        this.typologies = origin.getSupportedConnectorTopologies() //
                .stream() //
                .map(ConnectorTypology::from) //
                .map(ConnectorTypology::name) //
                .collect(Collectors.toSet());
        this.engines = origin.getSupportedExecutionEngines() //
                .stream() //
                .map(ExecutionEngine::toString) //
                .collect(Collectors.toSet());
    }

    private String buildImageUrl(String componentName) {
        return "/components/wizards/" + componentName + "/icon/" + WizardImageType.TREE_ICON_16X16;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getInputCompName() {
        return inputCompName;
    }

    public void setInputCompName(String inputCompName) {
        this.inputCompName = inputCompName;
    }

    public String getOutputCompName() {
        return outputCompName;
    }

    public void setOutputCompName(String outputCompName) {
        this.outputCompName = outputCompName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getTypologies() {
        return typologies;
    }

    public void setTypologies(Set<String> typologies) {
        this.typologies = typologies;
    }

    public Set<String> getExecutionEngines() {
        return engines;
    }

    public void setExecutionEngines(Set<String> engines) {
        this.engines = engines;
    }

    @Override
    public String toString() {
        return "DefinitionDTO{" + //
                "name='" + name + '\'' + //
                ", label='" + label + '\'' + //
                ", iconURL='" + iconURL + '\'' + //
                ", types=" + types + //
                ", inputCompName='" + inputCompName + '\'' + //
                ", outputCompName='" + outputCompName + '\'' + //
                ", type='" + type + '\'' + //
                ", typologies=" + typologies + //
                ", executionEngines=" + engines + //
                '}';
    }
}
