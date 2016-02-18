/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.talend.components.api.component.runtime;

import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.schema.Schema;

/**
 * Base class for defining input formats and creating a {@code Source} for reading the input.
 *
 * <p>
 * This class is not intended to be subclassed directly. Instead, to define a bounded source (a source which produces a
 * finite amount of input), subclass {@link BoundedSource}; to define an unbounded source, subclass
 * {@link UnboundedSource}.
 *
 * <p>
 * A {@code Source} passed to a {@code Read} transform must be {@code Serializable}. This allows the {@code Source}
 * instance created in this "main program" to be sent (in serialized form) to remote worker machines and reconstituted
 * for each batch of elements of the input {@code PCollection} being processed or for each source splitting operation. A
 * {@code Source} can have instance variable state, and non-transient instance variable state will be serialized in the
 * main program and then deserialized on remote worker machines.
 *
 * <p>
 * {@code Source} classes MUST be effectively immutable. The only acceptable use of mutable fields is to cache the
 * results of expensive operations, and such fields MUST be marked {@code transient}.
 *
 * <p>
 * {@code Source} objects should override {@link Object#toString}, as it will be used in important error and debugging
 * messages.
 *
 */
public interface Source extends SourceOrSink {

}
