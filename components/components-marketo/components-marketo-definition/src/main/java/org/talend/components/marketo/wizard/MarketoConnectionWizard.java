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
package org.talend.components.marketo.wizard;

import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;

public class MarketoConnectionWizard extends ComponentWizard {

    TMarketoConnectionProperties connProperties;

    MarketoCustomObjectsSchemasProperties coProps;

    public MarketoConnectionWizard(ComponentWizardDefinition definition, String repositoryLocation) {
        super(definition, repositoryLocation);
        connProperties = new TMarketoConnectionProperties("connection");
        connProperties.init();
        connProperties.setRepositoryLocation(repositoryLocation);
        addForm(connProperties.getForm(TMarketoConnectionProperties.FORM_WIZARD));

        coProps = new MarketoCustomObjectsSchemasProperties("customObjects");
        coProps.init();
        coProps.connection = connProperties;
        coProps.setRepositoryLocation(repositoryLocation);
        addForm(coProps.getForm(MarketoCustomObjectsSchemasProperties.FORM_CUSTOMOBJECTS));
    }

    public void setupProperties(TMarketoConnectionProperties properties) {
        connProperties.setupProperties();
        connProperties.copyValuesFrom(properties);
        coProps.connection = connProperties.getConnectionProperties();
    }
}
