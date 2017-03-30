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
package org.talend.components.azurestorage;

import java.util.UUID;

import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;

public class RuntimeContainerMock extends DefaultComponentRuntimeContainerImpl {

    @Override
    public String getCurrentComponentId() {

        return "component_" + UUID.randomUUID().toString().replace("-", "").substring(0, 3).toLowerCase();
    }

}
