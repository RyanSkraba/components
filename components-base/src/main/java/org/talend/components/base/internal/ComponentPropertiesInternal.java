package org.talend.components.base.internal;

import org.talend.components.base.properties.presentation.Form;
import org.talend.components.base.properties.presentation.Wizard;

import java.util.ArrayList;
import java.util.List;

public class ComponentPropertiesInternal {

    protected List<Form> forms;

    protected List<Wizard> wizards;

    public ComponentPropertiesInternal() {
        forms = new ArrayList<Form>();
        wizards = new ArrayList<Wizard>();
    }

    public List<Form> getForms() {
        return forms;
    }

    public List<Wizard> getWizards() {
        return wizards;
    }

    public Form getForm(String name) {
        for (Form f : forms) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

}
