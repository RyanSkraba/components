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

package org.talend.components.elasticsearch.runtime_2_4;

import java.io.IOException;

import org.apache.http.StatusLine;
import org.elasticsearch.client.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticsearchResponse {

    private StatusLine statusLine;

    private JsonNode entity;

    private static final ObjectMapper mapper = new ObjectMapper();

    public ElasticsearchResponse(Response response) throws IOException {
        this.statusLine = response.getStatusLine();
        if (response.getEntity() != null) {
            this.entity = parseResponse(response);
        }
    }

    private static JsonNode parseResponse(Response response) throws IOException {
        return mapper.readValue(response.getEntity().getContent(), JsonNode.class);
    }

    public JsonNode getEntity() {
        return entity;
    }

    public boolean isOk() {
        return statusLine.getStatusCode() == 200;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }
}
