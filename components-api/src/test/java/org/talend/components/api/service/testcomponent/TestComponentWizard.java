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
package org.talend.components.api.service.testcomponent;

import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.i18n.I18nMessageProvider;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;

public class TestComponentWizard extends ComponentWizard {

    public Properties props;

    public TestComponentWizard(ComponentWizardDefinition def, String location, I18nMessageProvider messageProvider) {
        super(def, location);

        props = new TestComponentProperties("root").init();
        addForm(props.getForm(Form.MAIN));
    }
}
