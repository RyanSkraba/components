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
package org.talend.components.multiruntime;

import static org.ops4j.pax.exam.CoreOptions.*;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.ComponentsPaxExamOptions;
import org.talend.components.api.service.ComponentService;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class MultiRuntimeTestIT extends AbstractMultiRuntimeComponentTests {

    @Inject
    ComponentService osgiCompService;

    @Configuration
    public Option[] config() {
        return options(composite(ComponentsPaxExamOptions.getOptions()), //
                linkBundle("org.talend.components-multiple-runtime-comp"));
    }

    @Override
    public ComponentService getComponentService() {
        return osgiCompService;
    }

}
