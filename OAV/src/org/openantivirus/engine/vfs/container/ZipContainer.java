/*
 * $Id: ZipContainer.java,v 1.1 2003/12/14 11:08:26 kurti Exp $
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
import java.util.zip.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.vfs.*;
import org.openantivirus.engine.vfs.entry.*;

/**
 * VfsContainer for ZIP files
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.1 $
 */
public class ZipContainer implements VfsContainer {
    
    private final ScanConfiguration configuration;
    private final VfsEntry entry;
    private final ZipFile zipFile;
    private final Enumeration entries;
    
    private ZipEntry nextEntry;
    
    public ZipContainer(VfsEntry entry, ScanConfiguration configuration)
    throws IOException {
        this.configuration = configuration;
        this.entry = entry;
        
        zipFile = new ZipFile(entry.getFile());
        entries = zipFile.entries();
        
        determineNext();
    }
        
    public boolean hasNext() {
        return nextEntry != null;
    }
    
    public VfsEntry next() throws IOException {
        final VfsEntry result = new TemporaryIsVfsEntry(
                entry.getName() + " >> zip:" + nextEntry.getName(),
                configuration,
                zipFile.getInputStream(nextEntry));
        determineNext();
        return result;
    }
    
    protected void determineNext() {
        do {
            if (!entries.hasMoreElements()) {
                nextEntry = null;
                break;
            }
            
            nextEntry = (ZipEntry) entries.nextElement();
        } while (nextEntry.isDirectory());
    }
    
    public void dispose() throws IOException {
        zipFile.close();
        nextEntry = null;
    }
}
