/*
 * $Id: DirectoryContainer.java,v 1.4 2005/09/03 16:16:40 kurti Exp $
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
import java.util.*;
import org.openantivirus.engine.*;
import org.openantivirus.engine.vfs.*;
import org.openantivirus.engine.vfs.entry.*;

/**
 * Container for plain directories; can strip parts of the path, e.g. for
 * temporary directories
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public class DirectoryContainer implements VfsContainer {
    
    /** to do list */
    private final LinkedList directories = new LinkedList();
    
    private final VfsEntry entry;
    private final ScanConfiguration scanConf;
    private final int removeLength;
    
    /** files in the current directory */
    private File[] currentFiles;
    
    /** position within the current directory */
    private int currentIndex;
    
    /** the next file (not directory) */
    private File nextFile;
    
    /** @param directory directory for this container */
    public DirectoryContainer(VfsEntry entry, ScanConfiguration scanConf)
    throws IOException {
        this(entry, scanConf, null);
    }
    /** @param directory directory for this container */
    public DirectoryContainer(VfsEntry entry,
                              ScanConfiguration scanConf,
                              String pathRemove)
                              throws IOException {
        this.entry        = entry;
        this.scanConf     = scanConf;
        this.removeLength = (pathRemove != null ? pathRemove.length() : 0);
        
        final File directory = entry.getFile();
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(
                    "not a directory: " + directory.getAbsolutePath());
        }
        
        handleDirectory(directory);
        determineNext();
    }
    
    /** @return if there are more VfsEntrys in this container */
    public boolean hasNext() {
        return nextFile != null;
    }
    
    /**
     * @return the next VfsEntry in "first files than directories"
     *         in depth search order
     * @throws IOException if the file cannot be accessed
     */
    public VfsEntry next() throws IOException {
        final VfsEntry result;
        if (removeLength == 0) {
            result = new FileVfsEntry(nextFile);
        } else {
            result = new FileVfsEntry(
                    nextFile,
                    entry.getName()
                    + FileVfsEntry.getRelativeName(nextFile)
                            .substring(removeLength));
        }
        
        determineNext();
        return result;
    }
    
    /** determines the next file (not directory) */
    protected void determineNext() throws IOException {
        File next;
        while ((next = getNext()) != null) {
            if (!scanConf.getBoolean("vfs.follow-symlinks")
                    &&  !next.getAbsolutePath().equals(
                            next.getCanonicalPath())) {
                return;
            }
            
            if (next.isFile()) {
                // we want a plain file, nothing else
                break;
            } else if (next.isDirectory()) {
                // we will get to you later
                directories.add(next);
            }
        }
        
        // this is either the next file or null if there is none
        nextFile = next;
    }

    /** @return the next file or directory */
    protected File getNext() {
        if (currentIndex < currentFiles.length) {
            return currentFiles[currentIndex++];
        }
        
        if (directories.isEmpty()) {
            return null;
        }
        
        handleDirectory((File) directories.removeFirst());
        return getNext();
    }
    
    /**
     * sets the file-array to the contents of this directory
     * and resets the index
     */
    protected void handleDirectory(File directory) {
        currentFiles = directory.listFiles();
        currentIndex = 0;
    }
    
    public void dispose() {
        currentFiles = null;
        nextFile = null;
    }
    
}
