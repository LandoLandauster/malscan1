/*
 * $Id: PackHeader.java,v 1.2 2005/09/03 16:16:40 kurti Exp $
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
 * PackHeader
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class PackHeader {
    public static final String VERSION =
        "$Id: PackHeader.java,v 1.2 2005/09/03 16:16:40 kurti Exp $";
    
    private static final byte[] UPX_MAGIC_LE32 = {'U', 'P', 'X', '!'};
    private static final int
            UPX_F_DOS_COM  = 1,
            UPX_F_DOS_SYS  = 2,
            UPX_F_DOS_EXE  = 3,
            UPX_F_DOS_EXEH = 7;
    
    private int version;
    private int format;
    private int method;
    private int level;
    private int filter;
    private int filter_cto;
    private long u_adler, c_adler;
    private int u_len, c_len;
    private int u_file_size;
    private int buf_offset;
    
    public boolean fillPackHeader(byte[] data) {
        int l = find(data, UPX_MAGIC_LE32);
        if (l == -1) {
            return false;
        }
        
        buf_offset = l;
        
        version = CStructure.getByte(data, l + 4);
        format  = CStructure.getByte(data, l + 5);
        method  = CStructure.getByte(data, l + 6);
        level   = CStructure.getByte(data, l + 7);
        filter_cto = 0;

        int off_filter = 0;
        if (format < 128) {
            u_adler = CStructure.getLE32(data, l + 8);
            c_adler = CStructure.getLE32(data, l + 12);
            if (format == UPX_F_DOS_COM || format == UPX_F_DOS_SYS) {
                u_len       = CStructure.getLE16(data, l + 16);
                c_len       = CStructure.getLE16(data, l + 18);
                u_file_size = u_len;
                off_filter  = 20;
            } else if (format == UPX_F_DOS_EXE || format == UPX_F_DOS_EXEH) {
                u_len       = CStructure.getLE24(data, l + 16);
                c_len       = CStructure.getLE24(data, l + 19);
                u_file_size = CStructure.getLE24(data, l + 22);
                off_filter  = 25;
            } else  {
                u_len       = (int) CStructure.getLE32(data, l + 16);
                c_len       = (int) CStructure.getLE32(data, l + 20);
                u_file_size = (int) CStructure.getLE32(data, l + 24);
                off_filter  = 28;
                filter_cto  = CStructure.getByte(data, l + 29);
            }
        } else {
            u_len       = (int) CStructure.getBE32(data, l + 8);
            c_len       = (int) CStructure.getBE32(data, l + 12);
            u_adler     = CStructure.getBE32(data, l + 16);
            c_adler     = CStructure.getBE32(data, l + 20);
            u_file_size = (int) CStructure.getBE32(data, l + 24);
            off_filter  = 28;
            filter_cto  = CStructure.getByte(data, l + 29);
        }
        
        if (version >= 10) {
            filter = CStructure.getByte(data, l + off_filter);
        } else if ((level & 128) == 0) {
            filter = 0;
        } else {
            level &= 0x7f;
            if (format == UPX_F_DOS_COM || format == UPX_F_DOS_SYS) {
                filter = 0x06;
            } else {
                filter = 0x26;
            }
        }
        
        level &= 0x0f;
        
        return true;
    }
    
    public boolean checkPackHeader(byte[] data) {
        if (version == 0xff) {
            System.out.println("cannot unpack UPX ;-)");
            return false;
        }
        
        final int hs = getPackHeaderSize();
        final int hlen = data.length - buf_offset;
        if (hlen <= 0 || hs > hlen) {
            System.err.println("header corrupted");
            return false;
        }
        
        if (version > 9) {
            if (data[buf_offset + hs - 1]
                    != getPackHeaderChecksum(data, hs - 1)) {
                System.err.println("header corrupted");
                return false;
            }
        }
        
        return true;
    }
    
    public int getVersion() {
        return version;
    }
    
    public int getCLength() {
        return c_len;
    }
    
    public int getULength() {
        return u_len;
    }
    
    public int getMethod() {
        return method;
    }
    
    public int getBufferOffset() {
        return buf_offset;
    }
    
    protected byte getPackHeaderChecksum(byte[] data, int len) {
        int buf = buf_offset;
        buf += 4;
        len -= 4;
        int c = 0;
        while (len-- > 0) {
            c += CStructure.getByte(data, buf++);
        }
        c %= 251;
        return (byte) c;
    }
    
    protected int getPackHeaderSize() {
        int n = 0;
        if (version <= 3) {
            n = 24;
        } else if (version <= 9) {
            if (format == UPX_F_DOS_COM || format == UPX_F_DOS_SYS) {
                n = 20;
            } else if (format == UPX_F_DOS_EXE || format == UPX_F_DOS_EXEH) {
                n = 25;
            } else {
                n = 28;
            }
        } else {
            if (format == UPX_F_DOS_COM || format == UPX_F_DOS_SYS) {
                n = 22;
            } else if (format == UPX_F_DOS_EXE || format == UPX_F_DOS_EXEH) {
                n = 27;
            } else {
                n = 32;
            }
        }
        if (n == 0) {
            System.err.println("unknown header version");
        }
        return n;
    }
    
    protected int find(byte[] data, byte[] pattern) {
ldata:  for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue ldata;
                }
            }
            return i;
        }
        return -1;
    }

    public long getC_adler() {
        return c_adler;
    }

    public int getFilter() {
        return filter;
    }

    public int getFilter_cto() {
        return filter_cto;
    }

    public long getU_adler() {
        return u_adler;
    }

    public int getU_file_size() {
        return u_file_size;
    }
}
