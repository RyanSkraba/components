package org.talend.components.marketo.runtime.client.rest.response;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.marketo.runtime.client.rest.type.LeadActivityRecord;

public class LeadActivitiesResult extends PaginateResult {

    private List<LeadActivityRecord> result;

    public List<LeadActivityRecord> getResult() {
        // ensure that result is never null
        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }

    public void setResult(List<LeadActivityRecord> result) {
        this.result = result;
    }
}
