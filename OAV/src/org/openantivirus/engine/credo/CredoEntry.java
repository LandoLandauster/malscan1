/*
 * $Id: CredoEntry.java,v 1.2 2005/09/03 16:16:40 kurti Exp $
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

package org.openantivirus.engine.credo;

import java.util.jar.*;
import java.io.*;

/**
 * An entry in a Credo file; it is a simple wrapper for JarEntry
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class CredoEntry {
    public static final String VERSION =
        "$Id: CredoEntry.java,v 1.2 2005/09/03 16:16:40 kurti Exp $";
    
    /**
     * Entry types
     */
    public static final int
        UNINITIALIZED = -1,
        UNKNOWN       = 0,
        STRINGS       = 1;
    
    /** containing file */
    private final JarInputStream jarInputStream;
    
    /** corresponding entry */
    private final JarEntry jarEntry;
    
    /** type of Entry; lazy initialization */
    private int type = UNINITIALIZED;
    
    public CredoEntry(JarInputStream jarInputStream, JarEntry jarEntry) {
        this.jarInputStream = jarInputStream;
        this.jarEntry       = jarEntry;
    }
    
    /**
     * @return InputStream of the entry's content
     */
    public InputStream getInputStream() {
        return jarInputStream;
    }
    
    /**
     * @return type of entry
     */
    public int getType() {
        if (type == UNINITIALIZED) {
            final String name = jarEntry.getName();
            if (name.endsWith(".strings")) {
                type = STRINGS;
            } else {
                type = UNKNOWN;
            }
        }
        return type;
    }
    
    public JarEntry getJarEntry() {
        return jarEntry;
    }
}
