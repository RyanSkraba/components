// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.component.runtime.WriterWithFeedback;

/**
 * Describe how a component can be connected in a job graph.
 *
 * In most cases, a component will act as only one of the following:
 * <ul>
 * <li>An input component: can only provide {@link #OUTGOING} connections.</li>
 * <li>An output component: can only provide {@link #INCOMING} connections.</li>
 * <li>A transformation or processing compononent: can only provide {@link #INCOMING_AND_OUTGOING} connections.</li>
 * </ul>
 *
 * However, it is possible that a single component can have more than one role -- an example is an output component that
 * can either be an endpoint in the job graph, but can optionally also provide "successful" and "rejected" records to
 * downstream components. The component can only assume one role at a time in the graph, but could provide different
 * runtimes in order depending on the selected topology.
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
     * {@link WriterWithFeedback} or ???? Just a note to think about -- transformation components will probably
     * implement any of java.function.Function (if java 8), org.talend.daikon.java8.Function (if not), Beam DoFn (when
     * we have beam libraries available) or a PTransform (when we're committed to running in a Beam runner). Also,
     * farther in the future, any of these can return a Beam PTransform!
     */
    INCOMING_AND_OUTGOING,
    /**
     * Component to be used as a Setup, it does not have any connectors setup. The runtime class must implement ???
     */
    NONE;

    /** Convenience constant for components that only support a single {@link #INCOMING} topology. */
    public static final Set<ConnectorTopology> INCOMING_ONLY = Collections.unmodifiableSet(EnumSet.of(INCOMING));

    /** Convenience constant for components that only support a single {@link #OUTGOING} topology. */
    public static final Set<ConnectorTopology> OUTGOING_ONLY = Collections.unmodifiableSet(EnumSet.of(OUTGOING));

    /** Convenience constant for components that only support a single {@link #INCOMING_AND_OUTGOING} topology. */
    public static final Set<ConnectorTopology> INCOMING_AND_OUTGOING_ONLY = Collections.unmodifiableSet(EnumSet
            .of(INCOMING_AND_OUTGOING));

    /** Convenience constant for components that only support a single {@link #NONE} topology. */
    public static final Set<ConnectorTopology> NONE_ONLY = Collections.unmodifiableSet(EnumSet.of(NONE));
}
