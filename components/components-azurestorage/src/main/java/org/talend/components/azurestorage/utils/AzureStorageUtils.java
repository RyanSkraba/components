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
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class comes from a decompilation of the {@code talend-azure-storage-utils-1.0.0.jar} provided by the
 * tAzureStoragePut component.
 *
 */
public class AzureStorageUtils {

    private transient static final Logger LOG = LoggerFactory.getLogger(AzureStorageUtils.class);

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
            map.put(file.getAbsolutePath(), keyParent);
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
                    LOG.error("No match file(" + key + ") exist!");
                }
            }
        }
        return fileMap;
    }
}