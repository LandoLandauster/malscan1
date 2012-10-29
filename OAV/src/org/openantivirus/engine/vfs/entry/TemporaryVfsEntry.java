/*
 * $Id: TemporaryVfsEntry.java,v 1.2 2004/05/20 13:04:50 kurti Exp $
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
import org.openantivirus.engine.vfs.*;

/**
 * Temporary entry that will be deleted
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public abstract class TemporaryVfsEntry extends VfsEntry {

    protected final OutputStream osTempFile;
    protected final TemporaryFile tempFile;
    protected final String name;
    protected final ScanConfiguration scanConf;
    
    public TemporaryVfsEntry(String name,
                             ScanConfiguration scanConf) throws IOException {
        this.name     = name;
        this.scanConf = scanConf;
        
        tempFile = new TemporaryFile(scanConf);
        osTempFile = new FileOutputStream(tempFile.getFile());
    }
    
    /** called if the VfsEntry is not needed any more  */
    public void dispose() throws IOException {
        osTempFile.close();
        tempFile.delete();
    }

    /** @return File this entry represents; if  */
    public File getFile() throws IOException {
        return tempFile.getFile();
    }

    public String getName() {
        return name;
    }

}
