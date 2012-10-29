/*
 * $Id: VfsEntry.java,v 1.3 2004/05/22 12:22:26 kurti Exp $
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
 * Portions created by the Initial Developer are Copyright (C) 2001-2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package org.openantivirus.engine.vfs;

import java.io.*;

/**
 * An entry in the vfs
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public abstract class VfsEntry {
    public static final int MAX_START_SIZE = 4096;

    public int[] foundParts;
    
    /** @return File this entry represents */
    public abstract File getFile() throws IOException;
    
    /**
     * @return the first block of bytes from the object; should be at least 4kB
     */
    public abstract byte[] getStart() throws IOException;
    
    /**
     * called if the VfsEntry is not needed any more; no method may be called
     * afterwards.
     */
    public abstract void dispose() throws IOException;
    
    /** @return (file-)name of the entry */
    public abstract String getName();
}
