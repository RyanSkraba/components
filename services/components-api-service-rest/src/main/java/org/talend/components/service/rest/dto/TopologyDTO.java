package org.talend.components.service.rest.dto;

import static org.talend.components.api.component.ConnectorTopology.*;

import org.talend.components.api.component.ConnectorTopology;

/**
 * DTO used to expose topologies  to the outside.
 */
public enum TopologyDTO {

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
    TopologyDTO(ConnectorTopology topology) {
        this.topology = topology;
    }

    /**
     * @param topology the topology to map.
     * @return the dto that matches the given topology.
     */
    static TopologyDTO from (ConnectorTopology topology) {
        for (TopologyDTO dto: values()) {
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
