// ============================================================================
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
// ============================================================================
package org.talend.components.api.wizard;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.i18n.TranslatableImpl;
import org.talend.daikon.properties.presentation.Form;

/**
 * A component wizard is used to create {@link ComponentProperties } objects. This class provide a set of {@link Form}
 * in their order of definition. Each form added to the wizard shall be displayed as single page. Each form shall belong
 * to the related ComponentPoperties. There are some ComponentProperties form lifecycle methods related to wizard see
 * {@link Properties}
 */
public abstract class ComponentWizard extends TranslatableImpl {

    private ComponentWizardDefinition definition;

    protected String repositoryLocation;

    protected List<Form> forms;

    /**
     * This shall be called by the service creation and is not supposed to be called by any client.
     * 
     * @param definition, wizard definition it is related to.
     * @param repositoryLocation, optional location used to store the data when wizard is finished.
     */
    public ComponentWizard(ComponentWizardDefinition definition, String repositoryLocation) {
        forms = new ArrayList<>();
        this.definition = definition;
        this.repositoryLocation = repositoryLocation;
    }

    /**
     * Add a form to the wizard, the order of the forms addition will define the order of the wizard pages.
     * 
     * @param form, a form to be handled by this wizard
     */
    public void addForm(Form form) {
        forms.add(form);
    }

    /**
     * @return the ordered list of forms handled by this wizard.
     */
    public List<Form> getForms() {
        return forms;
    }

    /**
     * The repository location used to store the {@link ComponentProperties} associated with this wizard, see
     * {@link ComponentWizardDefinition#supportsProperties(Class)}
     * 
     * @return, the location for storing the properties at the end of the wizard.
     */
    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    public ComponentWizardDefinition getDefinition() {
        return definition;
    }
}
