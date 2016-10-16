
package org.talend.components.fullexample.datastore;

import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * An example of a DatastoreDefinition.
 */
public class FullExampleDatastoreDefinition extends SimpleNamedThing
        implements DatastoreDefinition<FullExampleDatastoreProperties> {

    private static final String NAME = "FullExampleDatastore";

    public FullExampleDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public FullExampleDatastoreProperties createProperties() {
        return null;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(FullExampleDatastoreProperties properties, Object ctx) {
        return null;
    }
}
