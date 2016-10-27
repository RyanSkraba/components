package org.talend.components.snowflake.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class SnowflakeSink extends SnowflakeSourceOrSink implements Sink {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(SnowflakeSink.class);

    public SnowflakeSink() {
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validate = super.validate(container);
        // also check that the properties is the right type
        if (validate.getStatus() != Result.ERROR) {
            if (!(properties instanceof TSnowflakeOutputProperties)) {
                return new ValidationResult().setStatus(Result.ERROR)
                        .setMessage("properties should be of type :" + TSnowflakeOutputProperties.class.getCanonicalName());
            } // else this is the right type
        } // else already an ERROR
        return validate;
    }

    @Override
    public SnowflakeWriteOperation createWriteOperation() {
        return new SnowflakeWriteOperation(this);
    }

    /**
     * this should never be called before {@link #validate(RuntimeContainer)} is called but this should not be the case
     * anyway cause validate is called before the pipeline is created.
     *
     * @return the properties
     */
    public TSnowflakeOutputProperties getSnowflakeOutputProperties() {
        return (TSnowflakeOutputProperties) properties;
    }
}
