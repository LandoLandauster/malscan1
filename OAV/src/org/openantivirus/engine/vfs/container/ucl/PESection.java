/*
 * $Id: PESection.java,v 1.2 2004/05/01 14:36:10 kurti Exp $
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
 * Section of a PE file
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class PESection extends CStructure {
    
    public static final int SIZE = 40;
    
    /** Offsets */
    private static final int
        VADDRESS         = 12,
        RAW_DATA_POINTER = 20;
    
    /** Maximum length of section name */
    private static final int MAX_NAME_LENGTH = 8;
    
    public PESection(byte[] data) {
        super(data, SIZE);
    }
    
    public long getVAddress() {
        return getLE32(VADDRESS);
    }
    
    public String getName() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < MAX_NAME_LENGTH; i++) {
            final int value = getByte(i);
            if (value == 0) {
                break;
            }
            sb.append((char) value);
        }
        return sb.toString();
    }
    
    public long getRawDataPointer() {
        return getLE32(RAW_DATA_POINTER);
    }
}
