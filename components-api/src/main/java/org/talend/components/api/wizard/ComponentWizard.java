package org.talend.components.api.wizard;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;

/**
 * A component wizard is used to create {@link ComponentProperties } objects
 */
public abstract class ComponentWizard {

    protected String     userData;

    protected List<Form> forms;

    public ComponentWizard(String userData) {
        forms = new ArrayList<Form>();
        this.userData = userData;
    }

    public void addForm(Form form) {
        forms.add(form);
    }
    public List<Form> getForms() {
        return forms;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }
}
