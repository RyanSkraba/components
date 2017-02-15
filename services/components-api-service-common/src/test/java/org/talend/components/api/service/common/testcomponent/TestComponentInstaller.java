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
package org.talend.components.api.service.common.testcomponent;

import org.talend.components.api.ComponentInstaller;

import aQute.bnd.annotation.component.Component;

import com.google.auto.service.AutoService;

@AutoService(ComponentInstaller.class)
@Component(name = "test", provide = ComponentInstaller.class)
public class TestComponentInstaller implements ComponentInstaller {

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(new TestComponentFamilyDefinition());
    }
}
