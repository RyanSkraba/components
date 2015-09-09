package org.talend.components.api.properties;

import org.talend.components.api.AbstractNamedThing;

/**
 * A widget that appears in the UI that is not backed by a component property.
 */
public class PresentationItem extends AbstractNamedThing {

    public PresentationItem(String name, String displayName) {
        super(name, displayName);
    }

}
