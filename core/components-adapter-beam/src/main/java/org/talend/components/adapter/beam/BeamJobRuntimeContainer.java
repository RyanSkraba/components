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

import org.apache.beam.sdk.options.PipelineOptions;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.exception.TalendRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * Use on Driver side, to share data between components
 */
public class BeamJobRuntimeContainer implements RuntimeContainer {

    /** Use this container to share Beam {@link PipelineOptions}, this is the global key */
    public static final String PIPELINE_OPTIONS = "BeamPipelineOptions";

    private Map<String, Object> map = new HashMap();

    public BeamJobRuntimeContainer(PipelineOptions pipelineOptions) {
        map.put(PIPELINE_OPTIONS, pipelineOptions);
    }

    /**
     * Attention! there is no usage for beam compiler, but provide the basic implement as some component use other container implement in runtime.
     * @param componentId the identifier of the component within the container (as related to the calling component).
     * @param key an identifier for the type of data required (for example the connection information).
     * @return
     */
    @Override
    public Object getComponentData(String componentId, String key) {
        return map.get(componentId + key);
    }

    /**
     * Attention! there is no usage for beam compiler, but provide the basic implement as some component use other container implement in runtime.
     * @param componentId the identifier of the component within the container (as related to the calling component).
     * @param key an identifier fo the type of data required (for example the connection information).
     * @param data an Object, the data for the component.
     */
    @Override
    public void setComponentData(String componentId, String key, Object data) {
        map.put(componentId + key, data);
    }

    /**
     * Attention! there is no usage for beam compiler, but provide the basic implement as some component use other container implement in runtime.
     * @return
     */
    @Override
    public String getCurrentComponentId() {
        return null;
    }

    @Override
    public Object getGlobalData(String key) {
        return map.get(key);
    }
}
