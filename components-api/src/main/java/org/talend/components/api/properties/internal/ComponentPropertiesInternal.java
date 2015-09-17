package org.talend.components.api.properties.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.components.api.ComponentDesigner;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class ComponentPropertiesInternal {

    protected ComponentDesigner designer;

    protected List<Form> forms;

    protected ValidationResult validationResult;

    protected Map<SchemaElement, Object> propertyValues;

    public ComponentPropertiesInternal() {
        forms = new ArrayList<Form>();
        propertyValues = new HashMap();
    }

    public List<Form> getForms() {
        return forms;
    }

    public Form getForm(String name) {
        for (Form f : forms) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

    public void setValue(SchemaElement property, Object value) {
        propertyValues.put(property, value);
    }

    public Object getValue(SchemaElement property) {
        return propertyValues.get(property);
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
