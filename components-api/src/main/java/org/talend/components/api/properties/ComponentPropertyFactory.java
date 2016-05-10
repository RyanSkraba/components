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
package org.talend.components.api.properties;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.PropertyFactory;

/**
 * Make new {@link Property} objects.
 */
public class ComponentPropertyFactory {

    /**
     * Used if there are returns to set the "returns" property with a {@link Property} that contains the returns
     * properties.
     *
     * @return a {@link Property} that will contain the return properties
     */
    public static Property<String> newReturnsProperty() {
        // Container for the returns
        return PropertyFactory.newProperty(ComponentProperties.RETURNS);
    }

    /**
     * Adds a new return property.
     *
     * @param returns the {@link Property} returned by {@link #newReturnsProperty()}
     * @param returnProp the property to be added to the returns property
     * @param name the name of the returns property
     * @return a {@link Property}
     */
    public static <T> Property<T> newReturnProperty(Property<String> returns, Property<T> returnProp) {
        if (returns == null) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, new NullPointerException());
        }
        if (!ComponentProperties.RETURNS.equals(returns.getName())) {
            throw new ComponentException(ComponentsApiErrorCode.WRONG_RETURNS_TYPE_NAME,
                    ExceptionContext.build().put("name", returns.getName()));
        }
        returns.addChild(returnProp);
        return returnProp;
    }

}
