package org.talend.components.marketo.runtime.client.rest.type;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LeadChangeRecord extends MarketoAttributes {

    private Integer id;

    private Integer leadId;

    private Date activityDate;

    private Integer activityTypeId;

    private String activityTypeValue;

    private List<Map<String, String>> fields;

    private String marketoGUID;

    private Integer campaignId;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setLeadId(Integer leadId) {
        this.leadId = leadId;
    }

    public Integer getLeadId() {
        return this.leadId;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    public Date getActivityDate() {
        return this.activityDate;
    }

    public void setActivityTypeId(Integer activityTypeId) {
        this.activityTypeId = activityTypeId;
    }

    public String getActivityTypeValue() {
        return activityTypeValue;
    }

    public void setActivityTypeValue(String activityTypeValue) {
        this.activityTypeValue = activityTypeValue;
    }

    public Integer getActivityTypeId() {
        return this.activityTypeId;
    }

    public void setFields(List<Map<String, String>> fields) {
        this.fields = fields;
    }

    public List<Map<String, String>> getFields() {
        return this.fields;
    }

    public String getMarketoGUID() {
        return marketoGUID;
    }

    public void setMarketoGUID(String marketoGUID) {
        this.marketoGUID = marketoGUID;
    }

    public Integer getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }

    @Override
    public String toString() {
        return "LeadChangeRecord [id=" + id + ", leadId=" + leadId + ", activityDate=" + activityDate + ", activityTypeId="
                + activityTypeId + ", activityTypeValue=" + activityTypeValue + ", fields=" + fields + ", attributes="
                + getAttributes() + "]";
    }

}
