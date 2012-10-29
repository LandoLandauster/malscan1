/*
 * $Id: ArchiveContainer.java,v 1.4 2005/09/03 16:16:40 kurti Exp $
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
 * Container for archives, e.g. zip compressed files
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public abstract class ArchiveContainer implements VfsContainer {

    protected final VfsEntry entry;
    protected final TemporaryDirectory tempDir;
    
    private final String type;
    private DirectoryContainer directory;
    private final ScanConfiguration scanConf;
    private boolean initialized = false;
    
    protected ArchiveContainer(VfsEntry entry,
                               String type,
                               ScanConfiguration scanConf) {
        this.entry    = entry;
        this.type     = type;
        this.scanConf = scanConf;
        
        tempDir = new TemporaryDirectory(scanConf);
    }
    
    protected void init() throws IOException {
        try {
            extractArchive();
            directory = new DirectoryContainer(
                    new FileVfsEntry(tempDir.getDirectory(),
                            entry.getName() + type),
                    scanConf,
                    FileVfsEntry.getRelativeName(tempDir.getDirectory())
                        + File.separatorChar);
        } catch (Exception e) {
            tempDir.delete();
            throw new IOException("error while extracting: " + e.getMessage());
        }
        
        initialized = true;
    }
    
    public abstract void extractArchive() throws IOException;
    
    public boolean hasNext() throws IOException {
        if (!initialized) {
            init();
        }
        return directory.hasNext();
    }

    public VfsEntry next() throws IOException {
        return directory.next();
    }

    public void dispose() throws IOException {
        directory.dispose();
        tempDir.delete();
    }

    protected void runCommand(final String[] command) throws IOException {
        runCommand(command, null);
    }
    
    protected void runCommand(String[] command,
                              File runDirectory) throws IOException {
        runCommand(command, runDirectory, null);
    }
    
    protected void runCommand(String[] command,
                              File runDirectory,
                              OutputStream os) throws IOException {
        runCommand(command, runDirectory, null, true);
        
    }
    
    protected void runCommand(String[] command,
                              File runDirectory,
                              OutputStream os,
                              boolean closeStdIn) throws IOException {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command, null, runDirectory);
            if (closeStdIn) {
                process.getOutputStream().close();
            }
            if (os != null) {
                SingleFileContainer.copyStream(process.getInputStream(), os);
            }
            process.waitFor();
        } catch (InterruptedException ie) {
            // should not happen
            ie.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}
