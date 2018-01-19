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
package org.talend.components.google.drive.list;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class GoogleDriveListDefinition extends GoogleDriveComponentDefinition {

    public static final String COMPONENT_NAME = "tGoogleDriveList";

    public static final String RETURN_ID = "id";

    public static final String RETURN_KIND = "kind";

    public static final String RETURN_MIME_TYPE = "mimeType";

    public static final String RETURN_MODIFIED_TIME = "modifiedTime";

    public static final String RETURN_NAME = "name";

    public static final String RETURN_PARENTS = "parents";

    public static final String RETURN_SIZE = "size";

    public static final String RETURN_TRASHED = "trashed";

    public static final String RETURN_WEB_VIEW_LINK = "webViewLink";

    public static final Property<String> RETURN_ID_PROP = PropertyFactory.newString(RETURN_ID);

    public static final Property<String> RETURN_KIND_PROP = PropertyFactory.newString(RETURN_KIND);

    public static final Property<String> RETURN_MIME_TYPE_PROP = PropertyFactory.newString(RETURN_MIME_TYPE);

    public static final Property<String> RETURN_MODIFIED_TIME_PROP = PropertyFactory.newString(RETURN_MODIFIED_TIME);

    public static final Property<String> RETURN_NAME_PROP = PropertyFactory.newString(RETURN_NAME);

    public static final Property<String> RETURN_PARENTS_PROP = PropertyFactory.newString(RETURN_PARENTS);

    public static final Property<String> RETURN_SIZE_PROP = PropertyFactory.newString(RETURN_SIZE);

    public static final Property<String> RETURN_TRASHED_PROP = PropertyFactory.newString(RETURN_TRASHED);

    public static final Property<String> RETURN_WEB_VIEW_LINK_PROP = PropertyFactory.newString(RETURN_WEB_VIEW_LINK);

    public GoogleDriveListDefinition() {
        super(COMPONENT_NAME);
        setupI18N(new Property<?>[] { RETURN_ID_PROP, RETURN_KIND_PROP, RETURN_MIME_TYPE_PROP, RETURN_MODIFIED_TIME_PROP,
                RETURN_NAME_PROP, RETURN_PARENTS_PROP, RETURN_SIZE_PROP, RETURN_TRASHED_PROP, RETURN_WEB_VIEW_LINK_PROP });
    }
    //
    // @Override
    // public Property[] getReturnProperties() {
    // return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_ID_PROP, RETURN_KIND_PROP, RETURN_MIME_TYPE_PROP,
    // RETURN_MODIFIED_TIME_PROP, RETURN_NAME_PROP, RETURN_PARENTS_PROP, RETURN_SIZE_PROP, RETURN_TRASHED_PROP,
    // RETURN_WEB_VIEW_LINK_PROP };
    // }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return GoogleDriveListProperties.class;
    }

}
