package org.talend.components.marketo.runtime.client.rest.type;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LeadActivityRecord {

    private Integer id;

    private Integer leadId;

    private Date activityDate;

    private Integer activityTypeId;

    private String activityTypeValue;

    private Integer primaryAttributeValueId;

    private String primaryAttributeValue;

    private List<Map<String, String>> attributes;

    private String marketoGUID;

    private Integer campaignId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLeadId() {
        return leadId;
    }

    public void setLeadId(Integer leadId) {
        this.leadId = leadId;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    public Integer getActivityTypeId() {
        return activityTypeId;
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

    public Integer getPrimaryAttributeValueId() {
        return primaryAttributeValueId;
    }

    public void setPrimaryAttributeValueId(Integer primaryAttributeValueId) {
        this.primaryAttributeValueId = primaryAttributeValueId;
    }

    public String getPrimaryAttributeValue() {
        return primaryAttributeValue;
    }

    public void setPrimaryAttributeValue(String primaryAttributeValue) {
        this.primaryAttributeValue = primaryAttributeValue;
    }

    public List<Map<String, String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Map<String, String>> attributes) {
        this.attributes = attributes;
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
        return "LeadActivityRecord [id=" + id + ", leadId=" + leadId + ", activityDate=" + activityDate + ", activityTypeId="
                + activityTypeId + ", activityTypeValue=" + activityTypeValue + ", primaryAttributeValueId="
                + primaryAttributeValueId + ", primaryAttributeValue=" + primaryAttributeValue + ", attributes=" + attributes
                + "]";
    }

}
