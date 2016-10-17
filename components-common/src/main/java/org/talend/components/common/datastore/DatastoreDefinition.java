package org.talend.components.common.datastore;

import org.talend.components.api.RuntimableDefinition;
import org.talend.components.common.dataset.DatasetProperties;

/**
 * Placeholder for DatastoreDefinition
 */
public interface DatastoreDefinition<DatastorePropT extends DatastoreProperties>
        extends RuntimableDefinition<DatastorePropT, Object> {

    public DatasetProperties getDatasetProperties();
}
