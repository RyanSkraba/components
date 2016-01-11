/*
 * Copyright (c) 2015, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.talend.components.cassandra.io.dataflow.inputType1;

import com.google.cloud.dataflow.sdk.transforms.PTransform;
import com.google.cloud.dataflow.sdk.util.WindowingStrategy;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.PInput;
import org.talend.components.cassandra.tCassandraInput.tCassandraInputSparkProperties;

/**
 * Created by bchen on 16-1-9.
 */

public class CassandraIO {
    private CassandraIO() {

    }

    public static final class Read {

        private Read() {

        }

        public static Bound<String> named(String name) {
            return new Bound<String>().named(name);
        }

        public static class Bound<T> extends PTransform<PInput, PCollection<T>> {

            private final tCassandraInputSparkProperties properties;

            Bound() {
                this(null, null);
            }

            private Bound(String name, tCassandraInputSparkProperties properties) {
                super(name);
                this.properties = properties;
            }

            public Bound<T> named(String name) {
                return new Bound<>(name, null);
            }

            public Bound<T> from(tCassandraInputSparkProperties properties) {
                return new Bound<>(name, properties);
            }

            public tCassandraInputSparkProperties getProperties() {
                return properties;
            }

            @Override
            public PCollection<T> apply(PInput input) {
                if (properties == null) {
                    throw new IllegalStateException("need to set the properties of a CassandraIO.Read transform");
                }
                // Force the output's Coder to be what the read is using, and
                // unchangeable later, to ensure that we read the input in the
                // format specified by the Read transform.
                return PCollection.<T>createPrimitiveOutputInternal(
                        input.getPipeline(),
                        WindowingStrategy.globalDefault(),
                        PCollection.IsBounded.BOUNDED);
            }
        }
    }
}
