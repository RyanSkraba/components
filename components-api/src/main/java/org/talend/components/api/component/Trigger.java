// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.component;

/**
 * A trigger links two components together by a logical relationship. If the input component fulfill the condition, the
 * output component will be executed. They can use the context of the input component but will not transmit data to the
 * output component.
 *
 * Trigger connections are used to define the different subjobs of a job.
 */
public class Trigger extends AbstractComponentConnection {

    public enum TriggerType {
        ITERATE,
        SUBJOB_OK,
        SUBJOB_ERROR
    }

    protected TriggerType type;

    public Trigger(TriggerType type) {
        super(1, 1);
        this.type = type;
    }

    public Trigger(TriggerType type, int maxInput, int maxOutput) {
        super(maxInput, maxOutput);
        this.type = type;
    }

    public TriggerType getType() {
        return type;
    }

    public void setType(TriggerType type) {
        this.type = type;
    }

}
