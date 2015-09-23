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
package org.talend.components.api.properties;

public class ComponentConnector {

    public enum Type {
        FLOW,
        MAIN,
        ITERATE,
        REJECT,
        SUBJOB_OK,
        SUBJOB_ERROR,
        COMPONENT_OK,
        COMPONENT_ERROR,
        RUN_IF
    }

    protected Type type;

    protected int maxInput;

    protected int maxOutput;

    public ComponentConnector(Type type, int maxInput, int maxOutput) {
        this.type = type;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getMaxInput() {
        return maxInput;
    }

    public void setMaxInput(int maxInput) {
        this.maxInput = maxInput;
    }

    public int getMaxOutput() {
        return maxOutput;
    }

    public void setMaxOutput(int maxOutput) {
        this.maxOutput = maxOutput;
    }

}
