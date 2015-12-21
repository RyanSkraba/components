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
package org.talend.components.api.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO Currently an higly restrictive class, may become an interface, may become less restrictive, we have to think a
// lot about that.
// TODO find a better name
/**
 * The goal of this class is to allow the ComponentFacet to give back its result to the Runtime. This class may be
 * overided in order to support framework specific implementation.
 *
 */
public class ReturnObject {

    public static String MAIN = "MAIN";

    public static String ERROR = "ERROR";

    private Map<String, List<Map<String, Object>>> outputs = new HashMap<String, List<Map<String, Object>>>();

    public List<Map<String, Object>> getMainOutput() {
        return outputs.get(MAIN);
    }

    public void setMainOutput(Map<String, Object> value) {
        if (!outputs.containsKey(MAIN)) {
            outputs.put(MAIN, new ArrayList<Map<String, Object>>());
        }
        outputs.get(MAIN).add(value);
    }

    // TODO is this really insteresting to describe here?
    // maybe we can create a "returnRejectableObject"
    public List<Map<String, Object>> getErrorOutput() {
        return outputs.get(ERROR);
    }

    // TODO is this really insteresting to describe here?
    // maybe we can create a "returnRejectableObject"
    public void setErrorOutput(Map<String, Object> value) {
        if (!outputs.containsKey(ERROR)) {
            outputs.put(ERROR, new ArrayList<Map<String, Object>>());
        }
        outputs.get(ERROR).add(value);
    }

    // TODO is this really insteresting to describe here?
    // maybe we can create a "returnComplexObject"
    public List<Map<String, Object>> getOutput(String outputName) {
        return outputs.get(outputName);
    }

    // TODO is this really insteresting to describe here?
    // maybe we can create a "returnComplexObject"
    public void setOutput(String outputName, Map<String, Object> value) {
        if (!outputs.containsKey(outputName)) {
            outputs.put(outputName, new ArrayList<Map<String, Object>>());
        }
        outputs.get(outputName).add(value);

    }

}
