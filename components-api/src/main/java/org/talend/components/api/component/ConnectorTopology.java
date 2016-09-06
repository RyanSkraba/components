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
package org.talend.components.api.component;

import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.component.runtime.WriterWithFeedback;

/**
 * This will define the possible connections a component can handle. I'll be used to retrieve the right {@link RuntimeInfo}. One
 * single component can be compatible with multiple topologies but they are compatible with only one at a point in time.
 */
public enum ConnectorTopology {
    /**
     * Component that only have outgoing connectors. The runtime class must implement {@link Source}
     */
    OUTGOING,
    /**
     * Component that only have incoming connectors. The runtime class must implement {@link Sink}
     */
    INCOMING,
    /**
     * Transform component that have both incoming and outgoing connectors. The runtime class can implement
     * {@link WriterWithFeedback} or ????
     * Just a note to think about -- transformation components will probably implement any of java.function.Function (if java 8),
     * org.talend.daikon.java8.Function (if not), Beam DoFn (when we have beam libraries available) or a PTransform (when we're
     * committed to running in a Beam runner). Also, farther in the future, any of these can return a Beam PTransform!
     */
    INCOMING_AND_OUTGOING,
    /**
     * Component to be used as a Setup, it does not have any connectors setup. The runtime class must implement ???
     */
    NONE;
}
