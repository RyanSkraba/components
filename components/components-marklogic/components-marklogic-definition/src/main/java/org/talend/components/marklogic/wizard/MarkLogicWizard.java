// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.wizard;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.properties.presentation.Form;

import java.util.List;

public class MarkLogicWizard extends ComponentWizard {

    private MarkLogicConnectionProperties props;

    public MarkLogicWizard(ComponentWizardDefinition definition, String repositoryLocation) {
        super(definition, repositoryLocation);
        props = new MarkLogicConnectionProperties("props");
        props.init();
        props.setRepositoryLocation(repositoryLocation);

        addForm(props.getForm("wizardForm"));
    }

    public void setupProperties(MarkLogicConnectionProperties properties) {
        properties.init();
    }

    public void loadProperties(MarkLogicConnectionProperties anotherProps) {
        props.copyValuesFrom(anotherProps);
    }

    public boolean supportsProperties(ComponentProperties properties) {
        return properties instanceof MarkLogicConnectionProperties;
    }


}
