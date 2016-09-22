package org.talend.components.fullexample;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.fullexample.datastore.FullExampleDatastoreDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the FullExample family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + FullExampleFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class FullExampleFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "FullExample";

    public FullExampleFamilyDefinition() {
        super(NAME, new FullExampleInputDefinition(), new FullExampleDatastoreDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
