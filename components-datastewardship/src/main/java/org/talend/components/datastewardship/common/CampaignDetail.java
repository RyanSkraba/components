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
package org.talend.components.datastewardship.common;

import java.util.ArrayList;
import java.util.List;

public class CampaignDetail {

    private String name;

    private String label;

    private String type;

    private String creationDate;

    private List<String> states = new ArrayList<String>();

    private List<RecordField> fields = new ArrayList<RecordField>();

    public boolean isMerging() {
        return CampaignType.MERGING.toString().equals(type); // $NON-NLS-1$
    }

    /**
     * Getter for name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for label.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     * 
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Getter for type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * 
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for creationDate.
     * 
     * @return the creationDate
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creationDate.
     * 
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Getter for states.
     * 
     * @return the states
     */
    public List<String> getStates() {
        return states;
    }

    /**
     * Sets the states.
     * 
     * @param states the states to set
     */
    public void setStates(List<String> states) {
        this.states = states;
    }

    /**
     * Getter for fields.
     * 
     * @return the fields
     */
    public List<RecordField> getFields() {
        return fields;
    }

    /**
     * Sets the fields.
     * 
     * @param fields the fields to set
     */
    public void setFields(List<RecordField> fields) {
        this.fields = fields;
    }

    public static class RecordField {

        private String name;

        private String type;

        private boolean mandatory;

        private boolean readonly;

        public RecordField() {

        }

        /**
         * @param name
         * @param type
         * @param mandatory
         */
        public RecordField(String name, String type, boolean mandatory, boolean readonly) {
            super();
            this.name = name;
            this.type = type;
            this.mandatory = mandatory;
            this.readonly = readonly;
        }

        /**
         * Getter for name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name.
         * 
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Getter for type.
         * 
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the type.
         * 
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Getter for mandatory.
         * 
         * @return the mandatory
         */
        public boolean isMandatory() {
            return mandatory;
        }

        /**
         * Sets the mandatory.
         * 
         * @param mandatory the mandatory to set
         */
        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        /**
         * Getter for readonly.
         * 
         * @return the readonly
         */
        public boolean isReadonly() {
            return readonly;
        }

        /**
         * Sets the readonly.
         * 
         * @param readonly the readonly to set
         */
        public void setReadonly(boolean readonly) {
            this.readonly = readonly;
        }

        @Override
        public String toString() {
            return "RecordField{Name=" + name + ",Type=" + type + ",Mandatory=" + mandatory + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

    }

    @Override
    public String toString() {
        return "CampaignDetail{Name=" + name + ",Label=" + label + ",Type=" + type + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ",Creation Date=" + creationDate + ",States=" + states.toString() + ", Fields=" + fields.toString() + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
