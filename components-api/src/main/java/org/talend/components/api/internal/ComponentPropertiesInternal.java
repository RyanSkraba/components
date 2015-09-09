package org.talend.components.api.internal;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.ComponentDesigner;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Wizard;

public class ComponentPropertiesInternal {

    protected ComponentDesigner designer;

    protected List<Form>        forms;

    protected List<Wizard>      wizards;

    protected ValidationResult  validationResult;

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

    public ComponentDesigner getDesigner() {
        return designer;
    }

    public void setDesigner(ComponentDesigner designer) {
        this.designer = designer;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }
}
