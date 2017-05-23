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
package org.talend.components.azurestorage.queue.runtime;

import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class QueueMessage {

    private CloudQueueMessage msg;

    private int timeToLiveInSeconds;

    private int initialVisibilityDelayInSeconds;

    /**
     * @param msg
     * @param timeToLiveInSeconds
     * @param initialVisibilityDelayInSeconds
     */
    public QueueMessage(CloudQueueMessage msg, int timeToLiveInSeconds, int initialVisibilityDelayInSeconds) {
        super();
        this.msg = msg;
        this.timeToLiveInSeconds = timeToLiveInSeconds;
        this.initialVisibilityDelayInSeconds = initialVisibilityDelayInSeconds;
    }

    public CloudQueueMessage getMsg() {
        return msg;
    }

    public int getTimeToLiveInSeconds() {
        return timeToLiveInSeconds;
    }

    public int getInitialVisibilityDelayInSeconds() {
        return initialVisibilityDelayInSeconds;
    }

}
