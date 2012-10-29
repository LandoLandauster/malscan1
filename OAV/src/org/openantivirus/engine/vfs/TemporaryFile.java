/*
 * $Id: TemporaryFile.java,v 1.3 2005/09/03 16:16:40 kurti Exp $
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
 * Creates a temporary file that is deleted upon 'dispose'
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class TemporaryFile {
    private static final String TEMP_PREFIX = "file",
                                TEMP_SUFFIX = ".oav";
    
    private final File tempFile;
    
    public TemporaryFile(ScanConfiguration scanConfiguration)
    throws IOException {
        tempFile = File.createTempFile(
                TEMP_PREFIX,
                TEMP_SUFFIX,
                new File(scanConfiguration.getString("engine.tempdirectory")));
    }
    
    public File getFile() {
        return tempFile;
    }
    
    /** called if the VfsEntry is not needed any more  */
    public void delete() {
        tempFile.delete();
    }    
}
