/*
 * $Id: CStructure.java,v 1.4 2005/09/03 16:16:40 kurti Exp $
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
 * Wrapper to decode a C 'struct'
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public class CStructure {
    public static final String VERSION =
        "$Id: CStructure.java,v 1.4 2005/09/03 16:16:40 kurti Exp $";
    
    private final byte[] data;
    
    public CStructure(byte[] data, int size) {
        this.data = data;
        if (data.length != size) {
            throw new IllegalArgumentException("Datasize must be "
                                               + size);
        }
    }
    
    public int getByte(int index) {
        return getByte(data, index);
    }
    
    public int getLE16(int index) {
        return getLE16(data, index);
    }
    
    public long getLE32(int index) {
        return getLE32(data, index);
    }
    
    public static int getByte(byte[] data, int index) {
        return data[index] & 0xff;
    }
    
    public static int getLE16(byte[] data, int index) {
        return (getByte(data, index) | (getByte(data, index + 1) << 8));
    }

    public static int getBE16(byte[] data, int index) {
        return (getByte(data, index + 1) | (getByte(data, index) << 8));
    }

    public static int getLE24(byte[] data, int index) {
        return (getLE16(data, index)
                + ((getByte(data, index + 2)) << 16));
    }

    public static long getLE32(byte[] data, int index) {
        return (getLE16(data, index)
                + (((long) getLE16(data, index + 2)) << 16));
    }
    
    public static long getBE32(byte[] data, int index) {
        return (getBE16(data, index + 2)
                + (((long) getBE16(data, index)) << 16));
    }
}
