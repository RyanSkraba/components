package org.talend.components.api.properties;

import org.talend.components.api.SimpleNamedThing;
import org.talend.components.api.properties.presentation.Form;

/**
 * A widget that appears in the UI that is not backed by a component property.
 */
public class PresentationItem extends SimpleNamedThing {

    /**
     * The Form to show when this {@code PresentationItem} is activated (the button is pressed).
     */
    private Form formtoShow;

    public PresentationItem(String name, String displayName) {
        super(name, displayName);
    }

    public Form getFormtoShow() {
        return formtoShow;
    }

    public void setFormtoShow(Form formtoShow) {
        this.formtoShow = formtoShow;
    }

    public String toString() {
        return "Presentation Item: " + getName() + " - " + getTitle();
    }

}
