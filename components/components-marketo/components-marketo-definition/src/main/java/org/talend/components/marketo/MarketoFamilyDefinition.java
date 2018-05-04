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
package org.talend.components.marketo;

import aQute.bnd.annotation.component.Component;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.marketo.data.MarketoDatasetDefinition;
import org.talend.components.marketo.data.MarketoDatastoreDefinition;
import org.talend.components.marketo.data.MarketoInputDefinition;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecDefinition;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionDefinition;
import org.talend.components.marketo.tmarketoinput.TMarketoInputDefinition;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationDefinition;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputDefinition;
import org.talend.components.marketo.wizard.MarketoConnectionEditWizardDefinition;
import org.talend.components.marketo.wizard.MarketoConnectionWizardDefinition;

import com.google.auto.service.AutoService;

@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + MarketoFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class MarketoFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Marketo";

    public MarketoFamilyDefinition() {
        super(NAME, new TMarketoConnectionDefinition(), //
                new TMarketoInputDefinition(), //
                new TMarketoListOperationDefinition(), //
                new TMarketoOutputDefinition(), //
                new TMarketoBulkExecDefinition(), //
                new MarketoConnectionWizardDefinition(), //
                new MarketoConnectionEditWizardDefinition(), //
                //
                new MarketoDatastoreDefinition(), new MarketoDatasetDefinition(), new MarketoInputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
