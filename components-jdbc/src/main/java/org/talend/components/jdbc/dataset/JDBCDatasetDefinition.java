package org.talend.components.jdbc.dataset;

import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;

public class JDBCDatasetDefinition extends SimpleNamedThing implements DatasetDefinition<JDBCDatasetProperties> {

    public static final String NAME = "JDBCDataset";

    public JDBCDatasetDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(JDBCDatasetProperties properties, Object ctx) {
        return JDBCTemplate.createCommonRuntime(this.getClass().getClassLoader(), properties,
                JDBCDatasetRuntime.class.getCanonicalName());
    }

    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public Class<JDBCDatasetProperties> getPropertiesClass() {
        return JDBCDatasetProperties.class;
    }
}
