/*
 * $Id: CredoFile.java,v 1.1 2003/12/14 11:08:26 kurti Exp $
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

import java.util.*;
import java.util.jar.*;
import java.io.*;

/**
 * A file containing scanning information
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.1 $
 */
public class CredoFile {
    public static final String VERSION =
        "$Id: CredoFile.java,v 1.1 2003/12/14 11:08:26 kurti Exp $";
    
    /** Filename extension of Credo-files */
    public static final String EXTENSION = ".credo";
    
    private final JarInputStream jarInputStream;
    
    public CredoFile(File file) throws CredoException {
        if (!file.exists()) {
            jarInputStream = null;
            throw new CredoException("Credo-File does not exist");
        }
        if (!file.isFile()) {
            jarInputStream = null;
            throw new CredoException("Credo-File is not a file");
        }
        
        try {
            jarInputStream = new JarInputStream(new FileInputStream(file));
        } catch( IOException ioe) {
            throw new CredoException(ioe.getMessage());
        }
    }
    
    public CredoFile(InputStream is) throws CredoException {
        if (is == null) {
            throw new CredoException("Credo-File not found");
        }
        try {
            jarInputStream = new JarInputStream(is);
        } catch (IOException ioe) {
            throw new CredoException(ioe.getMessage());
        }
    }
    
    /**
     * Returns all entries contained in the credo file
     *
     * @return Iterator of CredoEntry
     */
    public Iterator entries() {
        return new CredoEntryIterator(jarInputStream);
    }
}
