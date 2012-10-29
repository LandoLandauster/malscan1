/*
 * $Id: EXEHeader.java,v 1.2 2004/05/01 14:36:10 kurti Exp $
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
 * EXEHeader
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class EXEHeader extends CStructure {

    public static final int SIZE = 64;
    
    /** Offsets */
    private static final int
        MZ                = 0,
        M512              = 2,
        P512              = 4,
        RELOCATION_OFFSET = 24,
        NEXT_E_POSITION   = 60;

    public EXEHeader(byte[] data) {
        super(data, SIZE);
    }

    public int getMZ() {
        return getLE16(MZ);
    }

    public int getM512() {
        return getLE16(M512);
    }

    public int getP512() {
        return getLE16(P512);
    }

    public int getRelocationOffset() {
        return getLE16(RELOCATION_OFFSET);
    }

    public long getNextEPos() {
        return getLE32(NEXT_E_POSITION);
    }
}
