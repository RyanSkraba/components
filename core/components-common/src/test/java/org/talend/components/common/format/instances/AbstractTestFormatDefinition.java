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
package org.talend.components.common.format.instances;

import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;

public abstract class AbstractTestFormatDefinition extends I18nDefinition implements Definition<AbstractTestFormatProperties> {

    public AbstractTestFormatDefinition(String name) {
        super(name);
    }

    @Override
    public Class<AbstractTestFormatProperties> getPropertiesClass() {
        return (Class<AbstractTestFormatProperties>) getPropertyClass();
    }

    public abstract Class<? extends AbstractTestFormatProperties> getPropertyClass();

    @Override
    public String getImagePath() {
        return null;
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        return null;
    }

    @Override
    public String getIconKey() {
        return null;
    }

}
