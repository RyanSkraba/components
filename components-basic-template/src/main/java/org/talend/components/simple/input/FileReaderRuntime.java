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
package org.talend.components.simple.input;

import java.util.Map;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;

/**
 * created by sgandon on 11 déc. 2015
 */
public class FileReaderRuntime extends ComponentRuntime {

    @Override
    public void inputBegin(ComponentProperties props) throws Exception {
    }

    @Override
    public Map<String, Object> inputRow() throws Exception {
        return null;
    }

    @Override
    public void inputEnd() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void outputBegin(ComponentProperties props) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void outputMain(Map<String, Object> row) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void outputEnd() throws Exception {
        // TODO Auto-generated method stub

    }

}
