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
package org.talend.components.jdbc.dataprep;

import java.util.ArrayList;
import java.util.List;

/**
 * the bean class for the database information
 *
 */
public class DBType {

    public String id;

    public String clazz;

    public String url;

    public List<String> paths = new ArrayList<>();

}
