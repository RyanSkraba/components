package org.talend.components.api.properties;

import org.talend.components.api.SimpleNamedThing;

/**
 * A widget that appears in the UI that is not backed by a component property.
 */
public class PresentationItem extends SimpleNamedThing {

    public PresentationItem(String name, String displayName) {
        super(name, displayName);
    }

}
