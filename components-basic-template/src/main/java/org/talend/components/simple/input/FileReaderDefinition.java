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
package org.talend.components.simple.input;

import java.io.InputStream;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.runtime.ComponentRuntime;

import aQute.bnd.annotation.component.Component;

/**
 * created by sgandon on 9 déc. 2015
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + FileReaderDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class FileReaderDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "FileReader"; //$NON-NLS-1$

    @Override
    public ComponentRuntime createRuntime() {
        return new FileReaderRuntime();
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        default:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        }
    }

    @Override
    public InputStream getMavenPom() {
        return null;
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public Class<?> getPropertyClass() {
        return FileReaderComponentProperties.class;
    }
}
