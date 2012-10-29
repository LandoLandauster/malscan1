/*
 * $Id: UpxContainer.java,v 1.3 2005/09/03 16:16:40 kurti Exp $
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
import org.openantivirus.engine.vfs.container.ucl.*;

/**
 * Handles UPX compressed files; first tries Java decompression code and if it
 * fails tries to decompress using the 'upx' command line tool.
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class UpxContainer extends SingleFileContainer {
    private final UPXDecompress upxDecompress;
    
    public UpxContainer(VfsEntry entry,
                        UPXDecompress upxDecompress,
                        ScanConfiguration scanConf)
    throws IOException {
        super(entry, "upx", scanConf);
        this.upxDecompress = upxDecompress;
        init();
    }
    
    public void extractFile() throws IOException {
        try {
            final OutputStream os = new FileOutputStream(tempFile.getFile());
            
            try {
                upxDecompress.decompress(os);
            } finally {
                os.close();
                upxDecompress.close();
            }
            
        } catch (Exception e) {
            // broken UPX files may break decompression
            e.printStackTrace();
            tempFile.delete();
            
            try {
                if (Runtime.getRuntime().exec(new String[] {
                            "upx",
                            "-dq",
                            "-o" + tempFile.getFile().getCanonicalPath(),
                            entry.getFile().getCanonicalPath()}).waitFor()
                        != 0) {
                    throw new IOException("Broken UPX file");
                }
            } catch (InterruptedException io) {
                throw new IOException("UPX decompress interrupted");
            }
        }
    }
    
}
