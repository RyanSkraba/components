// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.base.properties.presentation;

import org.talend.components.base.ComponentProperties;
import org.talend.components.base.properties.NamedThing;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the definitions for a wizard to help direct the UI of
 * one of more {@link Form} objects.
 */
public class Wizard extends NamedThing {

    private Form currentForm;

    private List<Form> forms;

    public Wizard(ComponentProperties props, String name, String description) {
        super(name, description);
        forms = new ArrayList<Form>();
        props.addWizard(this);
    }

    public static Wizard create(ComponentProperties props, String name, String displayName) {
        return new Wizard(props, name, displayName);
    }

    public Form getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(Form currentForm) {
        this.currentForm = currentForm;
    }

    public List<Form> getForms() {
        return forms;
    }

    public Wizard addForm(Form form) {
        forms.add(form);
        return this;
    }

}
