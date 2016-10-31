package org.talend.components.common.datastore;

import org.talend.components.api.RuntimableDefinition;
import org.talend.components.common.dataset.DatasetProperties;

/**
 * Placeholder for DatastoreDefinition
 */
public interface DatastoreDefinition<P extends DatastoreProperties> extends RuntimableDefinition<P, Object> {

    public DatasetProperties createDatasetProperties(DatastoreProperties storeProp);
}
