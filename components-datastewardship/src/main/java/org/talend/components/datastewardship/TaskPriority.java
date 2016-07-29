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
package org.talend.components.datastewardship;

public enum TaskPriority {
    Very_Low(0),
    Low(1),
    Medium(2),
    High(3),
    Very_High(4);
    
    private Integer value;
    
    private TaskPriority(Integer value) {
        this.value = value;
    }
    
    public Integer getValue(){
        return this.value;
    }
}
