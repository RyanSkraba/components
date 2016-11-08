package org.talend.components.common.io;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.properties.Properties;

public interface IOProperties<DatasetPropT extends DatasetProperties> extends Properties {

    public DatasetPropT getDatasetProperties();

    public void setDatasetProperties(DatasetPropT datasetProperties);

}
