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

package ${package}.integration;

import org.junit.Ignore;
import org.junit.Test;
import ${packageTalend}.${componentNameLowerCase}.definition.${componentNameClass}DatasetDefinition;

import java.util.UUID;

public class ${componentNameClass}DatasetTestIT {
    /**
     * Instance to test. Definitions are immutable.
     */
    private final ${componentNameClass}DatasetDefinition def = new ${componentNameClass}DatasetDefinition();

    //TODO
    @Test
    @Ignore("test this after create topic and prepare data using sandboxinstance")
    public void testBasic() throws Exception {

    }
}
