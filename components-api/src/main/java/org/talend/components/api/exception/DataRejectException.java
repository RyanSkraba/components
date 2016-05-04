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
package org.talend.components.api.exception;

import java.util.Map;

public class DataRejectException extends RuntimeException {

	private static final long serialVersionUID = -767063336424805519L;
	
	private Map<String,String> info;
	
	public DataRejectException(Map<String,String> info) {
    	this.info = info;
    }
	
	public Map<String,String> getRejectInfo() {
		return info;
	}

}
