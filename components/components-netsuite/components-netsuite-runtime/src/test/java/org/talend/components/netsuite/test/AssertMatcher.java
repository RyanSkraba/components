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

package org.talend.components.netsuite.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 *
 */
public abstract class AssertMatcher<T> extends BaseMatcher<T> {

    @Override
    public boolean matches(Object o) {
        try {
            doAssert((T) o);
            return true;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    protected abstract void doAssert(T target) throws Exception;

    @Override
    public void describeTo(Description description) {

    }
}
