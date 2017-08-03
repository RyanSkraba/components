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

public class TestFormatDefinition2Impl extends AbstractTestFormatDefinition {

    public TestFormatDefinition2Impl() {
        super("TestFormatDefinition2");
    }

    @Override
    public Class<? extends AbstractTestFormatProperties> getPropertyClass() {
        return TestFormatProperties2Impl.class;
    }

}
