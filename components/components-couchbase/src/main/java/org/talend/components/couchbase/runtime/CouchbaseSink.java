/*
 * Copyright (c) 2017 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.talend.components.couchbase.runtime;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.couchbase.output.CouchbaseOutputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class CouchbaseSink extends CouchbaseSourceOrSink implements Sink {

    private static final long serialVersionUID = 1313511127549129199L;

    private CouchbaseConnection connection;
    private String idFieldName;

    private boolean dieOnError;

    private boolean containsJson;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        if (properties instanceof CouchbaseOutputProperties) {
            CouchbaseOutputProperties outputProperties = (CouchbaseOutputProperties) properties;
            this.bootstrapNodes = outputProperties.bootstrapNodes.getStringValue();
            this.bucket = outputProperties.bucket.getStringValue();
            this.password = outputProperties.password.getStringValue();
            this.idFieldName = outputProperties.idFieldName.getStringValue();
            this.dieOnError = outputProperties.dieOnError.getValue();
            this.containsJson = outputProperties.containsJson.getValue();
            return ValidationResult.OK;
        }
        return new ValidationResult(Result.ERROR, "Wrong component properties, must be instanceof CouchbaseOutputProperties class");
    }

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new CouchbaseWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            connection = connect();
            return ValidationResult.OK;
        } catch (Exception ex) {
            return createValidationResult(ex);
        }
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public boolean isDieOnError() {
        return dieOnError;
    }

    public boolean getContainsJson() {
        return containsJson;
    }

    public CouchbaseConnection getConnection() {
        if (connection == null) {
            connection = connect();
        }
        return connection;
    }

    private CouchbaseConnection connect() {
        CouchbaseConnection connection = new CouchbaseConnection(bootstrapNodes, bucket, password);
        connection.connect();
        return connection;
    }
}
