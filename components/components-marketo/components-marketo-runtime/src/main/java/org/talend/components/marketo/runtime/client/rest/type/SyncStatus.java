package org.talend.components.marketo.runtime.client.rest.type;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class SyncStatus {

    private Integer id;

    private String status;

    private List<Map<String, String>> reasons;

    private String errorMessage;

    private String marketoGUID;

    private Integer seq;

    public SyncStatus() {
    }

    public SyncStatus(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Map<String, String>> getReasons() {
        return reasons;
    }

    public void setReasons(List<Map<String, String>> reasons) {
        this.reasons = reasons;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMarketoGUID() {
        return marketoGUID;
    }

    public void setMarketoGUID(String marketoGUID) {
        this.marketoGUID = marketoGUID;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getAvailableReason() {
        if (reasons == null || reasons.isEmpty()) {
            // sometimes when unrecoverable error happens, errorMessage will fit the reason
            if (!StringUtils.isEmpty(errorMessage)) {
                return errorMessage;
            }
            return "";
        }
        Map<String, String> m = reasons.get(0);
        String c = m.get("code");
        String msg = m.get("message");
        return String.format("[%s] %s.", c, msg);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SyncStatus{");
        sb.append("id=").append(id);
        sb.append(", marketoGUID='").append(marketoGUID).append('\'');
        sb.append(", seq=").append(seq);
        sb.append(", status='").append(status).append('\'');
        sb.append(", reasons=").append(reasons);
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
