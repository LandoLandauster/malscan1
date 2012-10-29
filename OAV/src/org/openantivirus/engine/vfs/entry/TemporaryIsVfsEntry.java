/*
 * $Id: TemporaryIsVfsEntry.java,v 1.2 2005/09/03 16:16:40 kurti Exp $
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

import org.openantivirus.engine.*;

/**
 * Creates a temporary file from an InputStream that is deleted on 'dispose'
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class TemporaryIsVfsEntry extends TemporaryVfsEntry {
    
    private final byte[] start;
    private final InputStream is;
    private boolean fileComplete;
    
    public TemporaryIsVfsEntry(String name,
                               ScanConfiguration scanConf,
                               InputStream is) throws IOException {
        super(name, scanConf);
        this.is   = is;
        
        // read the start of the stream
        int pos = 0;
        byte[] buffer = new byte[MAX_START_SIZE];
        int read;
        while ((read = is.read(buffer, pos, MAX_START_SIZE - pos)) != -1) {
            osTempFile.write(buffer, pos, read);
            pos += read;
            if (pos == MAX_START_SIZE) {
                break;
            }
        }
        
        fileComplete = (read == -1);
        
        // if the file is smaller than the start-buffer, we have to
        // create a smaller start-buffer and copy the file into it
        if (pos < MAX_START_SIZE) {
            start = new byte[pos];
            System.arraycopy(buffer, 0, start, 0, pos);
        } else {
            start = buffer;
        }
    }

    /**
     * @return the first block of bytes from the object; buffer size is the
     *         minimum of file size and 4096
     */
    public byte[] getStart() throws IOException {
        return start;
    }
    
    public void dispose() throws IOException {
        super.dispose();
        is.close();
    }
    
    public File getFile() throws IOException {
        if (!fileComplete) {
            final byte[] buffer = new byte[MAX_START_SIZE];
            
            // read the rest of the stream
            int read;
            while ((read = is.read(buffer)) != -1) {
                osTempFile.write(buffer, 0, read);
            }
            is.close();
            osTempFile.close();
            fileComplete = true;
        }
        
        return super.getFile();
    }
        
}