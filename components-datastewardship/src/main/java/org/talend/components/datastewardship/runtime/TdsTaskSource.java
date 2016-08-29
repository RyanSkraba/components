// ============================================================================
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
// ============================================================================
package org.talend.components.datastewardship.runtime;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.runtime.reader.TdsTaskReader;
import org.talend.components.datastewardship.tdatastewardshiptaskinput.TDataStewardshipTaskInputProperties;

public class TdsTaskSource extends TdsSource {

    private static final long serialVersionUID = -7153395345785814018L;

    /**
     * Campaign name
     */
    private String campaignName;

    /**
     * Campaign type
     */
    private CampaignType campaignType;

    /**
     * Task state
     */
    private String taskState;

    /**
     * Task assignee
     */
    private String taskAssignee;

    /**
     * Task priority
     */
    private Integer taskPriority;

    /**
     * Search query
     */
    private String searchQuery;

    /**
     * Retrieve golden record only
     */
    private boolean goldenOnly = true;

    /**
     * Batch size
     */
    private int batchSize = -1;

    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        super.initialize(container, properties);
        TDataStewardshipTaskInputProperties inputProperties = (TDataStewardshipTaskInputProperties) properties;
        this.campaignName = inputProperties.campaignName.getValue();
        this.campaignType = inputProperties.campaignType.getValue();
        this.taskState = inputProperties.searchCriteria.taskState.getValue();
        this.searchQuery = inputProperties.searchCriteria.searchQuery.getValue();
        // this.taskAssignee = inputProperties.searchCriteria.taskAssignee.getValue();
        // this.taskPriority = inputProperties.searchCriteria.taskPriority.getValue().getValue();
        this.goldenOnly = inputProperties.goldenOnly.getValue();
        this.batchSize = inputProperties.batchSize.getValue();

    }

    @Override
    public TdsTaskReader createReader(RuntimeContainer container) {
        return new TdsTaskReader(this);
    }

    /**
     * Getter for campaignName.
     * 
     * @return the campaignName
     */
    public String getCampaignName() {
        return campaignName;
    }

    /**
     * Getter for campaignType.
     * 
     * @return the campaignType
     */
    public CampaignType getCampaignType() {
        return campaignType;
    }

    /**
     * Getter for taskState.
     * 
     * @return the taskState
     */
    public String getTaskState() {
        return taskState;
    }

    /**
     * Getter for taskAssignee.
     * 
     * @return the taskAssignee
     */
    public String getTaskAssignee() {
        return taskAssignee;
    }

    /**
     * Getter for taskPriority.
     * 
     * @return the taskPriority
     */
    public Integer getTaskPriority() {
        return taskPriority;
    }

    /**
     * Getter for batchSize.
     * 
     * @return the batchSize
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Getter for searchQuery.
     * 
     * @return the searchQuery
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Getter for goldenOnly.
     * 
     * @return the goldenOnly
     */
    public boolean isGoldenOnly() {
        return goldenOnly;
    }

}
