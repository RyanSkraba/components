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
package org.talend.components.azurestorage.blob.tazurestoragecontainercreate;

import org.talend.components.azurestorage.blob.AzureStorageContainerProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TAzureStorageContainerCreateProperties extends AzureStorageContainerProperties {

    private static final long serialVersionUID = -1508935428835723716L;
    
    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");

    public enum AccessControl {
        Private,
        Public
    }

    public Property<AccessControl> accessControl = PropertyFactory.newEnum("accessControl", AccessControl.class); //$NON-NLS-1$

    public TAzureStorageContainerCreateProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(accessControl);
        mainForm.addRow(dieOnError);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        accessControl.setValue(AccessControl.Private);
    }
}
