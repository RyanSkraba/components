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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.common.datastore.DatastoreDefinition;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataStoreDefinitionDTO {

    /** Unique name to identify the data store. */
    private String name;

    /** Presentation name for UI. */
    private String label;

    /** Icon representing the DS. */
    private String iconURL;

    /** Types supported by the DS. */
    private List<String> types;

    /**  **/
    private String inputCompName;

    /**  **/
    private String outputCompName;

    public static DataStoreDefinitionDTO from(DatastoreDefinition origin) {
        DataStoreDefinitionDTO dto = new DataStoreDefinitionDTO();
        dto.setName(origin.getName());
        dto.setLabel(origin.getDisplayName());
        dto.setIconURL(buildImageUrl(origin.getName()));
        return dto;
    }

    private static String buildImageUrl(String componentName) {
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
}
