package org.talend.components.api;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.properties.presentation.Form;

/**
 * A component wizard is used to create {@link ComponentProperties } objects
 */
public abstract class ComponentWizard {

    protected List<Form> forms;

    public ComponentWizard() {
        forms = new ArrayList<Form>();
    }

    public List<Form> getForms() {
        return forms;
    }

}
