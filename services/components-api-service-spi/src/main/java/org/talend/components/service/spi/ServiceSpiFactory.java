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
package org.talend.components.service.spi;

import java.util.ServiceLoader;

import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;

public class ServiceSpiFactory {

    private static DefinitionRegistry defReg;

    private static ComponentService componentService;

    public static DefinitionRegistry getDefinitionRegistry() {
        if (defReg == null) {
            defReg = new DefinitionRegistry();
            for (ComponentInstaller installer : ServiceLoader.load(ComponentInstaller.class)) {
                installer.install(defReg);
            }
        }
        return defReg;
    }

    public static ComponentService getComponentService() {
        if (componentService == null) {
            componentService = new ComponentServiceImpl(getDefinitionRegistry());
        }
        return componentService;
    }
}
