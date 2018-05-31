// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake;

import org.apache.avro.Schema;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Used to specify property as one that is using query to retrieve data.
 *
 */
public abstract class SnowflakeGuessSchemaProperties extends SnowflakeConnectionTableProperties {

    public SnowflakeGuessSchemaProperties(String name) {
        super(name);
    }

    /**
     * Guess schema needs a query for retrieving metadata from database
     *
     * @return query to be executed
     */
    public abstract String getQuery();

    public ValidationResult validateGuessSchema() {
        try (SandboxedInstance sandboxI = SnowflakeDefinition.getSandboxedInstance(SnowflakeDefinition.SOURCE_OR_SINK_CLASS,
                SnowflakeDefinition.USE_CURRENT_JVM_PROPS)) {
            SnowflakeRuntimeSourceOrSink ss = (SnowflakeRuntimeSourceOrSink) sandboxI.getInstance();
            ss.initialize(null, this);
            Schema schema = ss.getSchemaFromQuery(null);
            table.main.schema.setValue(schema);
        } catch (Exception e) {
            return new ValidationResult(ValidationResult.Result.ERROR, e.getMessage());
        }
        return ValidationResult.OK;
    }

}