/*
 * $Id: PEHeader.java,v 1.3 2005/09/03 16:16:40 kurti Exp $
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

package org.openantivirus.engine.vfs.container.ucl;

/**
 * Header of a PE file
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class PEHeader extends CStructure {
    
    public static final int SIZE = 248;
    
    /** Offsets */
    private static final int
        OBJECTS      = 6,
        ENTRY        = 40,
        DDIRS_START  = 120,
        DDIRS_LENGTH = 8,
//        DDIRS_VADDR  = 0,
        DDIRS_SIZE   = 4;
        
    
    public PEHeader(byte[] data) {
        super(data, SIZE);
    }
    
    public int getObjects() {
        return getLE16(OBJECTS);
    }
    
    public long getEntry() {
        return getLE32(ENTRY);
    }
    
    public long getDDirsSize(int index) {
        return getLE32(DDIRS_START + DDIRS_LENGTH * index + DDIRS_SIZE);
    }
}
