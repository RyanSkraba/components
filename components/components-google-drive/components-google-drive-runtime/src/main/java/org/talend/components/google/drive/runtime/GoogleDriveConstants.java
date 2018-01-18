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
package org.talend.components.google.drive.runtime;

public class GoogleDriveConstants {

    public static final String DRIVE_ROOT_FOLDER = "root";

    public static final String ROOT_FOLDER_SEPARATOR = "/";

    public static final String Q_AND = " and ";

    public static final String Q_IN_PARENTS = "'%s' in parents";

    public static final String Q_MIME = "mimeType='%s'";

    public static final String Q_NOT_MIME = "mimeType!='%s'";

    public static final String Q_MIME_FOLDER = "mimeType='application/vnd.google-apps.folder'";

    public static final String Q_MIME_NOT_FOLDER = "mimeType!='application/vnd.google-apps.folder'";

    public static final String Q_NAME = "name='%s'";

    public static final String Q_TRASHED = "trashed=true";

    public static final String Q_NOT_TRASHED = "trashed=false";

    public static final String Q_HIDDEN = "hidden=true";

    public static final String Q_NOT_HIDDEN = "hidden=false";

    public static final String QUERY_MIME_FOLDER = Q_AND + Q_MIME_FOLDER;

    public static final String QUERY_MIME_NOT_FOLDER = Q_AND + Q_MIME_NOT_FOLDER;

    public static final String QUERY_NOTTRASHED_NAME = Q_NOT_TRASHED + Q_AND + Q_NAME;

    public static final String QUERY_NOTTRASHED_NAME_MIME = QUERY_NOTTRASHED_NAME + Q_AND + Q_MIME;

    public static final String QUERY_NOTTRASHED_NAME_NOTMIME = QUERY_NOTTRASHED_NAME + Q_AND + Q_NOT_MIME;

    public static final String QUERY_NOTTRASHED_NAME_NOTMIME_INPARENTS = QUERY_NOTTRASHED_NAME_NOTMIME + Q_AND + Q_IN_PARENTS;
}
