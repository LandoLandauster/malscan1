/*
 * $Id: SingleFileContainer.java,v 1.4 2005/09/03 16:16:40 kurti Exp $
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

package org.openantivirus.engine.vfs.container;

import java.io.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.vfs.*;
import org.openantivirus.engine.vfs.entry.*;

/**
 * Container for a single file, e.g. a gzip-compressed file
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public abstract class SingleFileContainer extends VfsEntry
                                          implements VfsContainer {

    protected final TemporaryFile tempFile;
    protected final VfsEntry entry;
    
    private final VfsEntry fileEntry;
    private final String type;
    private boolean read = false;
    private boolean initialized = false;
    
    protected SingleFileContainer(VfsEntry entry,
                                  String type,
                                  ScanConfiguration scanConf)
    throws IOException{
        this.entry = entry;
        this.type  = type;
        tempFile = new TemporaryFile(scanConf);
        fileEntry = new FileVfsEntry(tempFile.getFile());
    }
    
    protected void init() throws IOException {
        try {
            extractFile();
        } catch (Exception e) {
            tempFile.delete();
            throw new IOException("error while extracting: " + e.getMessage());
        }
        
        initialized = true;
    }
    
    public abstract void extractFile() throws IOException;
    
    public void dispose() throws IOException {
        tempFile.delete();
    }

    public File getFile() throws IOException {
        return tempFile.getFile();
    }

    public byte[] getStart() throws IOException {
        return fileEntry.getStart();
    }

    public boolean hasNext() throws IOException {
        if (!initialized) {
            init();
        }
        return !read;
    }

    public VfsEntry next() throws IOException {
        read = true;
        return this;
    }

    public static void copyStream(final InputStream is, final OutputStream os)
    throws IOException {
        final byte[] buffer = new byte[32768];
        int length;
        while ((length = is.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.close();
    }

    public String getName() {
        return entry.getName() + " >> " + type;
    }

    protected void runCommand(final String[] command, File workFile)
    throws IOException, FileNotFoundException {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            copyStream(process.getInputStream(),
                       new FileOutputStream(workFile));
            
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}
