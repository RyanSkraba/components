package org.talend.components.common.dataset;

import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.Properties;

/**
 * Placeholder for DatasetProperties.
 */
public interface DatasetProperties<DatastorePropT extends DatastoreProperties> extends Properties {

    public DatastorePropT getDatastoreProperties();

    public void setDatastoreProperties(DatastorePropT datastoreProperties);
}
