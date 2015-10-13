package org.talend.components.api.wizard;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.i18n.TranslatableImpl;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;

/**
 * A component wizard is used to create {@link ComponentProperties } objects
 */
public abstract class ComponentWizard extends TranslatableImpl {

    private ComponentWizardDefinition definition;

    protected String repositoryLocation;

    protected List<Form> forms;

    public ComponentWizard(ComponentWizardDefinition definition, String repositoryLocation) {
        forms = new ArrayList<>();
        this.definition = definition;
        this.repositoryLocation = repositoryLocation;
    }

    public void addForm(Form form) {
        forms.add(form);
    }

    public List<Form> getForms() {
        return forms;
    }

    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    public void setRepositoryLocation(String repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
    }

    public ComponentWizardDefinition getDefinition() {
        return definition;
    }
}
