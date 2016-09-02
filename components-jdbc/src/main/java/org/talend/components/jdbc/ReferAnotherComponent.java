package org.talend.components.jdbc;

import org.talend.components.api.properties.ComponentProperties;

public interface ReferAnotherComponent {

    String getReferencedComponentId();

    ComponentProperties getReferencedComponentProperties();

}
