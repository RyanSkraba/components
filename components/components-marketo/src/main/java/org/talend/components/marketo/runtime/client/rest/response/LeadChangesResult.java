package org.talend.components.marketo.runtime.client.rest.response;

import java.util.List;

import org.talend.components.marketo.runtime.client.rest.type.LeadChangeRecord;

public class LeadChangesResult extends PaginateResult {

    private List<LeadChangeRecord> result;
    public List<LeadChangeRecord> getResult() {
        return result;
    }
    public void setResult(List<LeadChangeRecord> result) {
        this.result = result;
    }
}
