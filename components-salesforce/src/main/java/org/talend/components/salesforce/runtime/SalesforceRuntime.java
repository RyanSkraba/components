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
package org.talend.components.salesforce.runtime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.talend.daikon.exception.TalendRuntimeException;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UpsertResult;

/**
 * Contains only runtime helper classes, mainly to do with logging.
 */
public class SalesforceRuntime {

    private SalesforceRuntime() {}

    public static StringBuilder addLog(Error[] resultErrors, String row_key, BufferedWriter logWriter) {
        StringBuilder errors = new StringBuilder("");
        if (resultErrors != null) {
            for (Error error : resultErrors) {
                errors.append(error.getMessage()).append("\n");
                if (logWriter != null) {
                    try {
                        logWriter.append("\tStatus Code: ").append(error.getStatusCode().toString());
                        logWriter.newLine();
                        logWriter.newLine();
                        logWriter.append("\tRowKey/RowNo: " + row_key);
                        if (error.getFields() != null) {
                            logWriter.newLine();
                            logWriter.append("\tFields: ");
                            boolean flag = false;
                            for (String field : error.getFields()) {
                                if (flag) {
                                    logWriter.append(", ");
                                } else {
                                    flag = true;
                                }
                                logWriter.append(field);
                            }
                        }
                        logWriter.newLine();
                        logWriter.newLine();

                        logWriter.append("\tMessage: ").append(error.getMessage());

                        logWriter.newLine();

                        logWriter.append("\t--------------------------------------------------------------------------------");

                        logWriter.newLine();
                        logWriter.newLine();
                    } catch (IOException ex) {
                        TalendRuntimeException.unexpectedException(ex);
                    }
                }
            }
        }
        return errors;
    }

    public static void populateResultMessage(Map<String, String> resultMessage, Error[] errors) {
        for (Error error : errors) {
            if (error.getStatusCode() != null) {
                resultMessage.put("StatusCode", error.getStatusCode().toString());
            }
            if (error.getFields() != null) {
                StringBuilder fields = new StringBuilder();
                for (String field : error.getFields()) {
                    fields.append(field);
                    fields.append(",");
                }
                if (fields.length() > 0) {
                    fields.deleteCharAt(fields.length() - 1);
                }
                resultMessage.put("Fields", fields.toString());
            }
            resultMessage.put("Message", error.getMessage());
        }
    }

    // FIXME - not sure what this is used for
    public static Map<String, String> readResult(Object[] results) throws Exception {
        Map<String, String> resultMessage = null;
        if (results instanceof SaveResult[]) {
            for (SaveResult result : (SaveResult[]) results) {
                resultMessage = new HashMap<>();
                if (result.getId() != null) {
                    resultMessage.put("id", result.getId());
                }
                resultMessage.put("success", String.valueOf(result.getSuccess()));
                if (!result.getSuccess()) {
                    populateResultMessage(resultMessage, result.getErrors());
                }
            }
            return resultMessage;
        } else if (results instanceof DeleteResult[]) {
            for (DeleteResult result : (DeleteResult[]) results) {
                resultMessage = new HashMap<>();
                if (result.getId() != null) {
                    resultMessage.put("id", result.getId());
                }
                resultMessage.put("success", String.valueOf(result.getSuccess()));
                if (!result.getSuccess()) {
                    populateResultMessage(resultMessage, result.getErrors());
                }
            }
            return resultMessage;
        } else if (results instanceof UpsertResult[]) {
            for (UpsertResult result : (UpsertResult[]) results) {
                resultMessage = new HashMap<>();
                if (result.getId() != null) {
                    resultMessage.put("id", result.getId());
                }
                resultMessage.put("success", String.valueOf(result.getSuccess()));
                resultMessage.put("created", String.valueOf(result.getCreated()));
                if (!result.getSuccess()) {
                    populateResultMessage(resultMessage, result.getErrors());
                }
            }
            return resultMessage;
        }
        return null;
    }

}
