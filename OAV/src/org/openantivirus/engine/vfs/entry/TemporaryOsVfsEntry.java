/*
 * $Id: TemporaryOsVfsEntry.java,v 1.1 2003/12/14 11:08:26 kurti Exp $
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
 * Temporary entry from an OutputStream
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.1 $
 */
public class TemporaryOsVfsEntry extends TemporaryVfsEntry {

    private final FileVfsEntry fileEntry;
    
    public TemporaryOsVfsEntry(String name, ScanConfiguration scanConf)
    throws IOException {
        super(name, scanConf);
        
        fileEntry = new FileVfsEntry(tempFile.getFile());
    }
    
    public OutputStream getOutputStream() {
        return osTempFile;
    }

    public byte[] getStart() throws IOException {
        return fileEntry.getStart();
    }
    
}
