package org.talend.components.api;

/**
 * Created by fupton on 9/8/15.
 */
public interface ComponentSchemaElement {

    public enum Type { STRING, INT, DATE }

    public String getName();

    public void setName(String name);

    public String getDescription();

    public void setDescription(String description);

    public Type getType();

    public void setType(Type type);

    public int getSize();

    public void setSize(int size);

}
