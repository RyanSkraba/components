package org.talend.components.service.rest.dto;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.PropertiesImpl;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO to represent a {@link Connector} on a {@link ComponentDefinition}.
 */
public class ConnectorDto {

    @JsonProperty("connectorType")
    private ConnectorTypology connectorType;

    @JsonProperty("flowType")
    private String flowType;

    public ConnectorDto(ConnectorTypology connectorType, Connector origin) {
        this.connectorType = connectorType;
        this.flowType = origin.getName();
    }

    /**
     * Return a list of all connectors for that component definition.
     *
     * @param origin The component definition to investigate.
     * @return A list of all connectors for that component definition.
     */
    public static List<ConnectorDto> createConnectorList(ComponentDefinition origin) {
        // Get the list of possible connectors.
        ComponentProperties properties = PropertiesImpl.createNewInstance(origin.getPropertiesClass(), "properties");
        List<ConnectorDto> connections = new ArrayList<>();

        for (Connector incoming : properties.getPossibleConnectors(false)) {
            connections.add(new ConnectorDto(ConnectorTypology.SINK, incoming));
        }
        for (Connector outgoing : properties.getPossibleConnectors(true)) {
            connections.add(new ConnectorDto(ConnectorTypology.SOURCE, outgoing));
        }

        return connections;
    }

    public ConnectorTypology getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorTypology connectorType) {
        this.connectorType = connectorType;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

}
