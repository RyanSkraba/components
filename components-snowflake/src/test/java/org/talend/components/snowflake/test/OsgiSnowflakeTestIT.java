// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.test;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.talend.components.api.ComponentsPaxExamOptions;
import org.talend.components.api.service.ComponentService;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputDefinition;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputDefinition;

import javax.inject.Inject;
import java.util.Arrays;

import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiSnowflakeTestIT extends SnowflakeTestIT {

    @Inject
    ComponentService osgiCompService;

    @Inject
    BundleContext bc;

    @Configuration
    public Option[] config() {
        return getSnowflakePaxExamOption();
    }

    static public Option[] getSnowflakePaxExamOption() {
        return options(composite(ComponentsPaxExamOptions.getOptions()), //
                linkBundle("org.talend.components-components-common-bundle"), //
                linkBundle("org.talend.components-components-common-tests").noStart(), //
                linkBundle("org.talend.components-components-snowflake-bundle"), //
                propagateSystemProperties("snowflake.account", "snowflake.password", "snowflake.warehouse", "snowflake.schema",
                        "snowflake.db", "snowflake.user"));
        //vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"));
    }

    @Override
    public ComponentService getComponentService() {
        return osgiCompService;
    }

    @Test
    public void checkComponentExists() {
        assertComponentIsRegistered(TSnowflakeInputDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TSnowflakeOutputDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TSnowflakeConnectionDefinition.COMPONENT_NAME);
    }

    //@Test
    public void showbundleContext() throws InvalidSyntaxException {
        System.out.println(" CLASS IS LOCATED :" + this.getClass().getResource(""));
        System.out.println(" ALL BUNDLES" + Arrays.toString(bc.getBundles()));
        ServiceReference<?>[] allServiceReferences = bc.getAllServiceReferences(null, null);
        System.out.println("ALL SERVICES : " + Arrays.toString(allServiceReferences));
        Bundle[] bundles = bc.getBundles();
        for (Bundle bnd : bundles) {
            System.out.println("Bundle :" + bnd.toString());
            if (bnd.getSymbolicName().equals("org.talend.components.snowflake")) {
                System.out.println("Bundle state:" + bnd.getState());
            }
        }
        System.out.println("component service: " + getComponentService());
    }
}
