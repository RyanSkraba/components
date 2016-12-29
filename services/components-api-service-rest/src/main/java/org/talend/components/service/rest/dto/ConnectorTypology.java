//==============================================================================
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
//==============================================================================
package org.talend.components.service.rest.dto;

import org.talend.components.api.component.ConnectorTopology;

import static org.talend.components.api.component.ConnectorTopology.*;

/**
 * DTO used to expose topologies to the outside world.
 */
public enum ConnectorTypology {

    /** source -> incoming*/
    SOURCE(INCOMING),
    /** sink -> outgoing. */
    SINK(OUTGOING),
    /** Transformer -> incoming and outgoing. */
    TRANSFORMER(INCOMING_AND_OUTGOING),
    /** configuration -> none. */
    CONFIGURATION(NONE);

    /** The connector topology to map. */
    private ConnectorTopology topology;

    /**
     * Default constructor.
     * @param topology the topology to map.
     */
    ConnectorTypology(ConnectorTopology topology) {
        this.topology = topology;
    }

    /**
     * @param topology the topology to map.
     * @return the dto that matches the given topology.
     */
    static ConnectorTypology from (ConnectorTopology topology) {
        for (ConnectorTypology dto: values()) {
            if (dto.topology == topology) {
                return dto;
            }
        }
        return null;
    }

    /**
     * @return the mapped topology.
     */
    public ConnectorTopology getTopology() {
        return topology;
    }
}
