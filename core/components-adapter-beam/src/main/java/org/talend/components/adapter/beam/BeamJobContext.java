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
package org.talend.components.adapter.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.PCollection;

/**
 * Provides a context for building into the current Beam {@link Pipeline} containing the job.
 *
 * This context includes all of the changes that have been added to the pipeline by components up to this point, and any
 * component building into the context is responsible for adding its results back into this context.
 */
public interface BeamJobContext {

    /**
     * Get the PCollection of data was built into the job by earlier components. This should be called for every
     * incoming connection into a component. Components that do not have incoming connections can use the
     * {@link #getPipeline()} method to obtain a starting point.
     * 
     * @param linkName The unique name for a connection between two components in the job graph.
     * @return The PCollection that contains the data that will flow over that connection when the job is run.
     */
    PCollection getPCollectionByLinkName(String linkName);

    /**
     * Save a PCollection of data that is the result of building a component into the job. This should be called exactly
     * once for every outgoing connection from a component.
     * 
     * @param linkName The unique name for a connection between two components in the job graph.
     * @param pCollection The PCollection that contains the data that will flow over that connection when the job is
     * run.
     */
    void putPCollectionByLinkName(String linkName, PCollection pCollection);

    /**
     * A port name identifies a connection with relation to a component. For example, the port might be 'IN', 'OUT',
     * 'MAIN' or 'REJECT', where the link name would be 'row1', 'row2', 'row3'.
     *
     * @param portName The names of the connection related to the component.
     * @return The unique name for a connection between two components in the job graph.
     */
    String getLinkNameByPortName(String portName);

    /**
     * @return The Beam Pipeline currently under construction in the job.
     */
    Pipeline getPipeline();

    /**
     * @return The Beam Pipeline Options currently under construction in the job.
     */
    PipelineOptions getPipelineOptions();

    /**
     * @return The Runtime container for share data between components on Driver.
     */
    BeamJobRuntimeContainer getRuntimeContainer();

}
