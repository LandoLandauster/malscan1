/*
 * $Id: WriteableScanConfiguration.java,v 1.4 2005/09/03 16:16:40 kurti Exp $
 * 
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OAV.
 *
 * The Initial Developer of the Original Code is Kurt Huwig <kurt@huwig.de>.
 * Portions created by the Initial Developer are Copyright (C) 2001-2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package org.openantivirus.engine;

import java.io.*;
import java.util.*;

/**
 * A ScanConfiguration that can be changed; can have a parent configuration to
 * look up entries that it does not have.
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public class WriteableScanConfiguration implements ScanConfiguration {

    private static final Collection BOOLEAN_SETTINGS = Arrays.asList(
            new String[] {
                    "engine.halt-on-malware-found",
                    "engine.use-trie",
                    "vfs.follow-symlinks",
                    "vfs.archive.enable",
                    "vfs.archive.ace",
                    "vfs.archive.ar",
                    "vfs.archive.arc",
                    "vfs.archive.arj",
                    "vfs.archive.bzip2",
                    "vfs.archive.cab",
                    "vfs.archive.compress",
                    "vfs.archive.cpio",
                    "vfs.archive.dact",
                    "vfs.archive.dar",
                    "vfs.archive.gzip",
                    "vfs.archive.lha",
                    "vfs.archive.lzo",
                    "vfs.archive.ppmd",
                    "vfs.archive.rar",
                    "vfs.archive.rpm",
                    "vfs.archive.shar",
                    "vfs.archive.tar",
                    "vfs.archive.tnef",
                    "vfs.archive.upx",
                    "vfs.archive.uuencode",
                    "vfs.archive.zip",
                    "vfs.archive.zoo"
            });
    
    private static final Collection INT_SETTINGS = Arrays.asList(
            new String[] {
                    "credo.level"
            });
    
    private static final Collection STRING_SETTINGS = Arrays.asList(
            new String[] {
                    "credo.directory",
                    "engine.tempdirectory"
            });
    
    private Map properties = new HashMap();

    private final ScanConfiguration parent;

    protected WriteableScanConfiguration() {
        parent = null;
    }

    public WriteableScanConfiguration(ScanConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent may not be null");
        }
        this.parent = parent;
    }
    
    public boolean getBoolean(String key) {
        return ((Boolean) getObject(key)).booleanValue();
    }
    
    public String getString(String key) {
        return (String) getObject(key);
    }

    public int getInt(String key) {
        return ((Integer) getObject(key)).intValue();
    }
    
    public Object getObject(String key) {
        final Object result = properties.get(key);

        if (result != null) {
            return result;
        }
        
        if (parent != null) {
            return parent.getObject(key);
        }
        throw new IllegalArgumentException("unknown key '" + key + "'");
    }

    public void putObject(String key, Object value) {
        properties.put(key, value);
    }
    
    public void putBoolean(String key, boolean value) {
        putObject(key, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public void putString(String key, String value) {
        putObject(key, value);
    }

    public void putInt(String key, int value) {
        putObject(key, new Integer(value));
    }
    
    public void putAny(String key, String value) {
        // guess the type
        if (BOOLEAN_SETTINGS.contains(key)) {
            putBoolean(key, "yes".equals(value));
        } else if (INT_SETTINGS.contains(key)) {
            putInt(key, Integer.parseInt(value));
        } else if (STRING_SETTINGS.contains(key)){
            putString(key, value);
        } else {
            System.err.println("Unknown key: " + key);
        }
    }
    
    public void loadFile(String filename) throws IOException {
        final Properties settings = new Properties();
        settings.load(new FileInputStream(filename));
        
        for (Iterator it = settings.entrySet().iterator();
             it.hasNext(); ) {
            
            Map.Entry setting = (Map.Entry) it.next();
            putAny((String) setting.getKey(), (String) setting.getValue());
        }
    }

}
