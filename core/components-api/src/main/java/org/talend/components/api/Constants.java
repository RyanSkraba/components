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
package org.talend.components.api;

public class Constants {
    static {
        // j11 workaround assuming this constant will be loaded pretty early
        if (System.getProperty("sun.boot.class.path") == null) {
            System.setProperty("sun.boot.class.path", System.getProperty("java.class.path"));
        }
    }

    /**
     * Used in the name attribute of the aQute.bnd.annotation.component.Component annotation of a
     * {@link ComponentInstaller}. This name attribute is only used during service discovery.
     */
    public static final String COMPONENT_INSTALLER_PREFIX = "installer$$"; //$NON-NLS-1$
}
