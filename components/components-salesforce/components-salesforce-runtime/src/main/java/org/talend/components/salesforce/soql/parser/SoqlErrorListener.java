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
package org.talend.components.salesforce.soql.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.talend.components.salesforce.common.SalesforceErrorCodes;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;

public class SoqlErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg,
            RecognitionException e) {


        TalendRuntimeException.build(SalesforceErrorCodes.INVALID_SOQL) //
                .put(ExceptionContext.KEY_MESSAGE, "error at line " + line + " and position " + charPositionInLine)//
                .throwIt();
    }

}
