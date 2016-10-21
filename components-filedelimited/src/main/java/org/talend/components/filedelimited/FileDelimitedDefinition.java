
package org.talend.components.filedelimited;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;

public abstract class FileDelimitedDefinition extends AbstractComponentDefinition {

    public FileDelimitedDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    public String getMavenGroupId() {
        return "org.talend.components";
    }

    public String getMavenArtifactId() {
        return "components-filedelimited";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return FileDelimitedProperties.class;
    }

    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { FileDelimitedProperties.class };
    }

}
