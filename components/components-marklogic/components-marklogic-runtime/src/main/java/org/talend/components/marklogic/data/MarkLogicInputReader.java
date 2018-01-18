package org.talend.components.marklogic.data;

import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marklogic.runtime.input.MarkLogicCriteriaReader;

/**
 *
 *
 */
public class MarkLogicInputReader extends MarkLogicCriteriaReader {

    public MarkLogicInputReader(BoundedSource source, RuntimeContainer container, MarkLogicDatasetProperties properties) {
        super(source, container, properties);
    }

    @Override
    protected Setting prepareSettings(ComponentProperties inputProperties) {
        MarkLogicDatasetProperties properties = (MarkLogicDatasetProperties) inputProperties;

        return new Setting(
                properties.main.schema.getValue(),
                properties.criteria.getValue(), -1, properties.pageSize.getValue(),
                properties.useQueryOption.getValue(), properties.queryLiteralType.getValue(),
                properties.queryOptionName.getValue(), properties.queryOptionLiterals.getValue(),
                properties.getDatastoreProperties().isReferencedConnectionUsed());
    }

}
