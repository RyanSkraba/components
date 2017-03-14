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

package org.talend.components.netsuite.test;

import javax.xml.ws.handler.MessageContext;

/**
 *
 */
public class MessageContextHolder {

    private static ThreadLocal<MessageContext> holder = new ThreadLocal<>();

    public static void set(MessageContext messageContext) {
        holder.set(messageContext);
    }

    public static MessageContext get() {
        return holder.get();
    }

    public static void remove() {
        holder.remove();
    }
}
