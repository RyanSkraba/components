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
package org.talend.components.azurestorage.blob;

import org.talend.components.azurestorage.AzureStorageProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class AzureStorageContainerProperties extends AzureStorageProperties {

    private static final long serialVersionUID = 2687440470676027837L;

    /** container - the AzureStorage remote container name. */
    public Property<String> container = PropertyFactory.newString("container").setRequired(true);

    public AzureStorageContainerProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        container.setValue("");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form main = getForm(Form.MAIN);
        main.addRow(container);
    }

}
