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

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 *
 */
public abstract class CouchbaseSourceOrSink implements SourceOrSink {

    protected String bootstrapNodes;
    protected String bucket;
    protected String password;

    protected static ValidationResult createValidationResult(Exception ex) {
        String message;
        if (ex.getMessage() == null || ex.getMessage().isEmpty()) {
            message = ex.toString();
        } else {
            message = ex.getMessage();
        }
        return new ValidationResult(ValidationResult.Result.ERROR, message);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }


}
