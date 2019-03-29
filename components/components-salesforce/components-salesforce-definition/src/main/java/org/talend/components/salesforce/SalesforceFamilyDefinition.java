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
package org.talend.components.salesforce;

import org.osgi.service.component.annotations.Component;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.salesforce.dataprep.SalesforceInputDefinition;
import org.talend.components.salesforce.dataset.SalesforceDatasetDefinition;
import org.talend.components.salesforce.datastore.SalesforceDatastoreDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforcegetdeleted.TSalesforceGetDeletedDefinition;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampDefinition;
import org.talend.components.salesforce.tsalesforcegetupdated.TSalesforceGetUpdatedDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulkexec.TSalesforceOutputBulkExecDefinition;

import com.google.auto.service.AutoService;

/**
 * Install all of the definitions provided for the Salesforce family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + SalesforceFamilyDefinition.NAME, service = ComponentInstaller.class)
public class SalesforceFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Salesforce";

    public SalesforceFamilyDefinition() {
        super(NAME,
                // Components
                new TSalesforceBulkExecDefinition(), new TSalesforceConnectionDefinition(), new TSalesforceGetDeletedDefinition(),
                new TSalesforceGetServerTimestampDefinition(), new TSalesforceGetUpdatedDefinition(),
                new TSalesforceInputDefinition(), new TSalesforceOutputDefinition(), new TSalesforceOutputBulkDefinition(),
                new TSalesforceOutputBulkExecDefinition(),
                // Component wizards
                new SalesforceConnectionWizardDefinition(),
                //TODO remove the edit one as it's duplicated
                new SalesforceConnectionEditWizardDefinition(), 
                new SalesforceModuleWizardDefinition(),
                // Datastore, Dataset and the component
                new SalesforceDatastoreDefinition(), new SalesforceDatasetDefinition(), new SalesforceInputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }

}
