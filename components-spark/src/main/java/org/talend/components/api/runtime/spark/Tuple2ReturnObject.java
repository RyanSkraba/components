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
package org.talend.components.api.runtime.spark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.components.api.runtime.ReturnObject;

import scala.Tuple2;

public class Tuple2ReturnObject extends ReturnObject {

    public static boolean MAIN_KEY = true;

    public static boolean ERROR_KEY = false;

    private List<Tuple2<Boolean, Map<String, Object>>> outputs = new ArrayList<Tuple2<Boolean, Map<String, Object>>>();

    /**
     * On a Tuple2ReturnObject, the getMainOutput will List<Tuple<Object>> into a List<Object>. This is a slow
     * processing, please use getTupleOutput.
     */
    @Deprecated
    @Override
    public List<Map<String, Object>> getMainOutput() {
        List<Map<String, Object>> mainOutputs = new ArrayList<Map<String, Object>>();
        for (Tuple2<Boolean, Map<String, Object>> output : outputs) {
            // this is checking if true == true, but it is more explicit this way
            if (output._1() == MAIN_KEY) {
                mainOutputs.add(output._2());
            }
        }
        return mainOutputs;
    }

    @Override
    public void setMainOutput(Map<String, Object> value) {
        outputs.add(new Tuple2<Boolean, Map<String, Object>>(MAIN_KEY, value));
    }

    /**
     * On a Tuple2ReturnObject, the getMainOutput will List<Tuple<Object>> into a List<Object>. This is a slow
     * processing, please use getTupleOutput.
     */
    @Deprecated
    @Override
    public List<Map<String, Object>> getErrorOutput() {
        List<Map<String, Object>> errorOutputs = new ArrayList<Map<String, Object>>();
        for (Tuple2<Boolean, Map<String, Object>> output : outputs) {
            // this is checking if false == false, but it is more explicit this way
            if (output._1() == ERROR_KEY) {
                errorOutputs.add(output._2());
            }
        }
        return errorOutputs;
    }

    @Override
    public void setErrorOutput(Map<String, Object> value) {
        outputs.add(new Tuple2<Boolean, Map<String, Object>>(ERROR_KEY, value));
    }

    @Override
    public List<Map<String, Object>> getOutput(String outputName) {
        throw new RuntimeException("You cannot use the method \"getOutput\" on a KeyValueReturnObject"); //$NON-NLS-1$
    }

    @Override
    public void setOutput(String outputName, Map<String, Object> value) {
        throw new RuntimeException("You cannot use the method \"setOutput\" on a KeyValueReturnObject"); //$NON-NLS-1$
    }

    public List<Tuple2<Boolean, Map<String, Object>>> getTupleOutput() {
        return outputs;
    }
}
