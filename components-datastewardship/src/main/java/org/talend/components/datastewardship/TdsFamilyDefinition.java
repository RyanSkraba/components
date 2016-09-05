package org.talend.components.datastewardship;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.datastewardship.tdatastewardshipcampaigncreate.TDataStewardshipCampaignCreateDefinition;
import org.talend.components.datastewardship.tdatastewardshiptaskinput.TDataStewardshipTaskInputDefinition;
import org.talend.components.datastewardship.tdatastewardshiptaskoutput.TDataStewardshipTaskOutputDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the Data Stewardship family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + TdsFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class TdsFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "DataStewardship";

    public TdsFamilyDefinition() {
        super(NAME, new TDataStewardshipCampaignCreateDefinition(), new TDataStewardshipTaskInputDefinition(),
                new TDataStewardshipTaskOutputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
