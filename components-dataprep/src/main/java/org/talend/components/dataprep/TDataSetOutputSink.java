package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by stavytskyi on 4/19/16.
 */
public class TDataSetOutputSink implements Sink {

    TDataSetOutputProperties properties;

    @Override
    public WriteOperation<?> createWriteOperation() {
        DataPrepConnectionHandler connectionHandler = null;
        if (properties.mode.getStringValue().equals("create"))
            connectionHandler = new DataPrepConnectionHandler(properties.url.getStringValue(),
                    properties.login.getStringValue(), properties.pass.getStringValue(),
                    "create", properties.dataSetName.getStringValue());
        return new TDataSetWriteOperation(this, connectionHandler);
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer, ComponentProperties componentProperties) {
        this.properties = (TDataSetOutputProperties) componentProperties;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtimeContainer) {
        //TODO: Validate all input data
        return ValidationResult.OK;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }
}
