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
package org.talend.components.api.component.runtime;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.exception.TalendRuntimeException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A suggested implementation of the result of a series of writes which is returned by {@link Writer#close()} and
 * consumed by {@link WriteOperation#finalize(Iterable, RuntimeContainer)}.
 *
 * This can also be used for reads which is returned by {@link Reader#getReturnValues()}.
 */
public class Result implements Serializable {

    private static final long serialVersionUID = 8670579213592463768L;

    protected String uId;

    public int totalCount;

    public int successCount;

    public int rejectCount;

    public String getuId() {
        return this.uId;
    }

    public Result() {
    }

    /**
     * Create a result for a single writer.
     *
     * @param uId unique Id provided when calling {@link Writer#open(String)}
     */
    public Result(String uId) {
        this.uId = uId;
    }

    /**
     * See {@link #Result(String)}
     */
    public Result(String uId, int totalCount) {
        this(uId);
        this.totalCount = totalCount;
    }

    /**
     * See {@link #Result(String)}
     */
    public Result(String uId, int totalCount, int successCount, int rejectCount) {
        this(uId, totalCount);
        this.successCount = successCount;
        this.rejectCount = rejectCount;
    }

    /**
     * Accumulate the given {@code WriterResult} objects and return the map with the record counts.
     * 
     * @param writerResults the results to accumulate
     * @return a {@link Map} with the standard record count results
     */
    public static <T extends Result> Map<String, Object> accumulateAndReturnMap(Iterable<T> writerResults) {
        Result returnResult = null;
        for (T result : writerResults) {
            if (returnResult == null) {
                try {
                    returnResult = result.getClass().newInstance();
                } catch (InstantiationException e) {
                    TalendRuntimeException.unexpectedException(e);
                } catch (IllegalAccessException e) {
                    TalendRuntimeException.unexpectedException(e);
                }
            }
            returnResult.add(result);
        }
        return returnResult.toMap();
    }

    public void add(Result result) {
        totalCount += result.getTotalCount();
        successCount += result.getSuccessCount();
        rejectCount += result.getRejectCount();
    }

    public static Map<String, Object> createResultMap(int totalCount, int successCount, int rejectCount) {
        Map<String, Object> map = new HashMap();
        map.put(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT, totalCount);
        map.put(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT, successCount);
        map.put(ComponentDefinition.RETURN_REJECT_RECORD_COUNT, rejectCount);
        return map;
    }

    public Map<String, Object> toMap() {
        return createResultMap(totalCount, successCount, rejectCount);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getRejectCount() {
        return rejectCount;
    }

    public String toString() {
        return "total: " + totalCount + " success: " + successCount + " reject: " + rejectCount;
    }

}
