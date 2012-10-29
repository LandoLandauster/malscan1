/*
 * $Id: FileVfsEntry.java,v 1.3 2005/09/03 16:16:40 kurti Exp $
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

package org.openantivirus.engine.vfs.entry;

import java.io.*;

import org.openantivirus.engine.vfs.*;

/**
 * Entry for a file
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class FileVfsEntry extends VfsEntry {
    
    /** lazy initialized array with the start of the file */
    private byte[] start = null;
    
    /** file this entry represents */
    private final File file;
    private final String name;
    
    public FileVfsEntry(File file) {
        this.file = file;
        name = getRelativeName(file);
    }
    
    public FileVfsEntry(File file, String name) {
        this.file = file;
        this.name = name;
    }
    
    public void dispose() throws IOException {
        start = null;
    }
    
    /** @return (file-)name of the entry */
    public String getName() {
        return name;
    }
    
    /** @return File this entry represents */
    public File getFile() {
        return file;
    }
    
    /**
     * @return the first block of bytes from the object; buffer size is the
     *         minimum of file size and 4096
     */
    public byte[] getStart() throws IOException {
        if (start == null) {
            final RandomAccessFile raf = new RandomAccessFile(file, "r");
            start = new byte[Math.min(MAX_START_SIZE, (int) file.length())];
            raf.readFully(start);
            raf.close();
        }
        return start;
    }
    
    public static String getRelativeName(File file) {
        final String parent = file.getParent();
        return parent != null
               ? parent + File.separatorChar + file.getName()
               : file.getName();
    }
}
