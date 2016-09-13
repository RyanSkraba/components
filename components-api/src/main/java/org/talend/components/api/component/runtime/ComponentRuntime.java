// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.component.runtime;

import java.io.Serializable;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.ValidationResult;

/**
 * Base interface for defining properties and methods common to Runtime interfaces, like {@link SourceOrSink} or
 * {@link ComponentDriverInitialization}.
 * 
 * <p>
 * A {@code ComponentRuntime} may be passed between partititions for distributed operation and therefore must be
 * {@code Serializable}. This allows the {@code ComponentRuntime} instance created in this "main program" to be sent (in
 * serialized form) to remote worker machines and reconstituted for each batch of elements being processed. A
 * {@code ComponentRuntime} can have instance variable state, and non-transient instance variable state will be
 * serialized in the main program and then deserialized on remote worker machines.
 *
 */
public interface ComponentRuntime extends Serializable {

    /**
     * Gets the ComponentProperties associated with this runtime class in order to configure it. This class also checks
     * that all required properties are properly set and coherent. No integrity check must be done like checking a
     * remote connection or checking a file exists here. This method shall be called before the job or flow is executed
     * and shall prevent execution if the ValidationResult has an ERROR status.
     * <p>
     * This method is expected to be called in a distributed environment on driver node before the runtime is serialized
     * and sent to worker nodes for execution.
     * </p>
     * 
     * @param container container instance to get access or share data with the running container.
     * @param properties used to configure this runtime class.
     * @return ValidationResult status of the validation, if the status is ERROR then the flow or job shall be stopped.
     */
    ValidationResult initialize(RuntimeContainer container, ComponentProperties properties);

}
