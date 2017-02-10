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
package org.talend.components.azurestorage.blob.tazurestoragecontainerdelete;

import org.talend.components.azurestorage.blob.AzureStorageContainerProperties;
import org.talend.daikon.properties.presentation.Form;

public class TAzureStorageContainerDeleteProperties extends AzureStorageContainerProperties {

    private static final long serialVersionUID = -8409678756536686919L;

    public TAzureStorageContainerDeleteProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(dieOnError);
    }
}
