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

import org.talend.components.api.container.RuntimeContainer;

/**
 * Base interface for defining containing input properties and creating a {@link Reader} for reading the input.
 *
 * <p>
 * See {@link SourceOrSink}.
 *
 */
public interface Source extends SourceOrSink {

    /**
     * Returns a new {@link Reader} that reads from this source.
     */
    public abstract Reader createReader(RuntimeContainer container);

}
