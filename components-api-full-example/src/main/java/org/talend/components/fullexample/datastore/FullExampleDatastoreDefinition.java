
package org.talend.components.fullexample.datastore;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.common.datastore.DatastoreProperties;
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
        return (FullExampleDatastoreProperties) new FullExampleDatastoreProperties(null).init();
    }

    @Override
    public RuntimeInfo getRuntimeInfo(FullExampleDatastoreProperties properties, Object ctx) {
        return null;
    }

    @Override
    public DatasetProperties createDatasetProperties(DatastoreProperties storeProp) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Full example datastore";
    }

    @Override
    public String getTitle() {
        return "Full example datastore";
    }

    @Override
    public String getImagePath() {
        return "/org/talend/components/fullexample/fullExample_icon32.png";
    }
}
