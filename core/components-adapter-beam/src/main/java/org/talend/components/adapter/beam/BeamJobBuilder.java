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

/**
 * Permit a component runtime instance to build itself into a Beam Pipeline.
 *
 * The
 * {@link org.talend.components.api.RuntimableDefinition#getRuntimeInfo(org.talend.daikon.properties.Properties, Object)}
 * can be used to create an object instance from a {@link org.talend.daikon.properties.Properties}. If that instance
 * implements this interface, it knows how to build itself into a Beam {@link org.apache.beam.sdk.Pipeline}.
 */
public interface BeamJobBuilder {

    /**
     * Builds the processing into the Beam job using the context provided.
     * 
     * @param ctx All of the information available for the job running in the current Beam
     * {@link org.apache.beam.sdk.Pipeline}, at the time this component is added. The component is responsible for
     * keeping the context in a known state.
     */
    void build(BeamJobContext ctx);
}
