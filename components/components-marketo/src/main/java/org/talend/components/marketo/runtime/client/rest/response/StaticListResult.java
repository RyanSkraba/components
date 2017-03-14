package org.talend.components.marketo.runtime.client.rest.response;

import java.util.List;

import org.talend.components.marketo.runtime.client.rest.type.ListRecord;

public class StaticListResult extends RequestResult {

    private List<ListRecord> result;
    public List<ListRecord> getResult() {
        return result;
    }
    public void setResult(List<ListRecord> result) {
        this.result = result;
    }
}
