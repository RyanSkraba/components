package org.talend.components.api.internal;

import org.talend.components.api.ComponentSchemaElement;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ComponentSchemaElementImpl implements ComponentSchemaElement {

    public String name;
    public String description;

    public Type type;

    public int size;

    protected List<ComponentSchemaElement> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<ComponentSchemaElement> getChildren() {
        return children;
    }

    public void setChildren(List<ComponentSchemaElement> children) {
        this.children = children;
    }

    public void addChild(ComponentSchemaElement child) {
        if (children == null)
            children = new ArrayList<ComponentSchemaElement>();
        children.add(child);
    }

}
