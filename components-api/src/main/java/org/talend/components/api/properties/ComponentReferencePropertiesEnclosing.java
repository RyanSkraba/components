package org.talend.components.api.properties;

/**
 * Used when {@link ComponentReferenceProperties} is used to call back to the enclosing {@link ComponentProperties}
 * after processing the associated {@link org.talend.daikon.properties.presentation.Widget}.
 */
public interface ComponentReferencePropertiesEnclosing {

    /**
     * Called after the value of the widget has changed.
     */
    void afterReferencedComponent();

}
