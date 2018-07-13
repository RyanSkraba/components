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
package org.talend.components.azurestorage.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.microsoft.azure.storage.OperationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This class comes from a decompilation of the {@code talend-azure-storage-utils-1.0.0.jar} provided by the
 * tAzureStoragePut component.
 *
 */
public class AzureStorageUtils {

    private transient static final Logger LOG = LoggerFactory.getLogger(AzureStorageUtils.class);

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(AzureStorageUtils.class);

    private static OperationContext talendOperationContext;

    private static final String USER_AGENT_KEY = "User-Agent";

    private static final String USER_AGENT_VALUE = "APN/1.0 Talend/7.1 TCOMP/0.24";

    public static OperationContext getTalendOperationContext() {
        if (talendOperationContext == null) {
            talendOperationContext = new OperationContext();
            HashMap<String, String> talendUserHeaders = new HashMap<>();
            talendUserHeaders.put(USER_AGENT_KEY, USER_AGENT_VALUE);
            talendOperationContext.setUserHeaders(talendUserHeaders);
        }

        return talendOperationContext;
    }

    class LocalFileFilter implements FileFilter {

        private final String mask;

        LocalFileFilter(String str) {
            this.mask = str;
        }

        @Override
        public boolean accept(File pathname) {
            if (pathname == null || !pathname.isFile()) {
                return false;
            }
            return Pattern.compile(this.mask).matcher(pathname.getName()).find();
        }
    }

    /**
     *
     */
    public Map<String, String> genAzureObjectList(File file, String keyParent) {
        Map<String, String> map = new HashMap<String, String>();
        if (file.isDirectory()) {
            if (!(keyParent == null || "".equals(keyParent)
                    || keyParent.trim().lastIndexOf("/") == keyParent.trim().length() - 1)) {
                keyParent = new StringBuilder(String.valueOf(keyParent)).append("/").toString();
            }
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    map.putAll(genAzureObjectList(f,
                            new StringBuilder(String.valueOf(keyParent)).append(f.getName()).append("/").toString()));
                } else {
                    map.put(f.getAbsolutePath(), new StringBuilder(String.valueOf(keyParent)).append(f.getName()).toString());
                }
            }
        } else {
            throw new IllegalArgumentException(i18nMessages.getMessage("error.invalidDirectory"));
        }
        return map;
    }

    /**
     *
     */
    public Map<String, String> genFileFilterList(List<Map<String, String>> list, String localdir, String remotedir) {
        if (remotedir != null) {
            if (!("".equals(remotedir) || remotedir.trim().lastIndexOf("/") == remotedir.trim().length() - 1)) {
                remotedir = new StringBuilder(String.valueOf(remotedir)).append("/").toString();
            }
        }
        Map<String, String> fileMap = new HashMap<String, String>();
        for (Map<String, String> map : list) {
            for (String key : map.keySet()) {
                String tempdir = localdir;
                String dir = null;
                String mask = key.replaceAll("\\\\", "/");
                int i = mask.lastIndexOf(47);
                if (i != -1) {
                    dir = mask.substring(0, i);
                    mask = mask.substring(i + 1);
                }
                if (dir != null) {
                    if (!"".equals(dir)) {
                        tempdir = new StringBuilder(String.valueOf(tempdir)).append("/").append(dir).toString();
                    }
                }
                mask = mask.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
                String finalMask = mask;
                File[] listings = null;
                File file = new File(tempdir);
                if (file.isDirectory()) {
                    listings = file.listFiles(new LocalFileFilter(finalMask));
                }
                if (listings != null && listings.length > 0) {
                    String localFilePath = "";
                    String newObjectKey = "";
                    int m = 0;
                    while (true) {
                        int length = listings.length;
                        if (m >= length) {
                            break;
                        }
                        if (listings[m].getName().matches(mask)) {
                            localFilePath = listings[m].getAbsolutePath();
                            if (map.get(key) == null || map.get(key).length() <= 0) {
                                newObjectKey = new StringBuilder(String.valueOf(remotedir)).append(listings[m].getName())
                                        .toString();
                            } else {
                                newObjectKey = new StringBuilder(String.valueOf(remotedir)).append(map.get(key)).toString();
                            }
                            fileMap.put(localFilePath, newObjectKey);
                        }
                        m++;
                    }
                } else {
                    LOG.error(i18nMessages.getMessage("error.FileNotExist", key));
                }
            }
        }
        return fileMap;
    }


    public static String getStudioNameFromProperty(final String inputString) {
        // FIXME : move this method to tcomp
        StringBuilder outputString = new StringBuilder();
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }

        for (int i = 0; i < inputString.length(); i++) {
            Character c = inputString.charAt(i);
            outputString.append(Character.isUpperCase(c) && i > 0 ? "_" + c : c); //$NON-NLS-1$
        }
        return outputString.toString().toUpperCase(Locale.ENGLISH);
    }
}