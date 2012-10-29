/*
 * $Id: TemporaryDirectory.java,v 1.2 2004/05/30 00:52:45 kurti Exp $
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

package org.openantivirus.engine.vfs;

import java.io.*;

import org.openantivirus.engine.*;

/**
 * Creates a temporary directory that is deleted upon 'dispose'
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class TemporaryDirectory {
    private final File tempDir;
    
    public TemporaryDirectory(ScanConfiguration scanConfiguration) {
        final File baseDir = new File(
                scanConfiguration.getString("engine.tempdirectory"));
        
        long id = System.currentTimeMillis();
        File dir;
        do {
            dir = new File(baseDir, Long.toString(id++));
        }
        while (!dir.mkdir());
        
        tempDir = dir;
    }
    
    public File getDirectory() {
        return tempDir;
    }
    
    /** called if the VfsEntry is not needed any more  */
    public void delete() throws IOException {
        delete(tempDir);
    }
    
    protected void delete(File directory) throws IOException {
        final File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                if (file.isDirectory()) {
                    delete(file);
                }
                file.delete();
            }
        }
        directory.delete();
    }
    
}
