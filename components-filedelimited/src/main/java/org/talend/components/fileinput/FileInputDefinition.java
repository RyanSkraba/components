
package org.talend.components.fileinput;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;

/**
 * The FileInputDefinition acts as an entry point for all of services that a
 * component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
public class FileInputDefinition extends AbstractComponentDefinition {

    public FileInputDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Input" }; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "file-input";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return FileInputProperties.class;
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

}
