package org.talend.components.api.properties;

/**
 * A holder for a name and corresponding label. Used for certain widgets.
 */
public class NameAndLabel {

    public String name;

    public String label;

    public NameAndLabel(String name, String label) {
        this.name = name;
        this.label = label;
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

    public String toString() {
        return name + "/" + label;
    }
}
