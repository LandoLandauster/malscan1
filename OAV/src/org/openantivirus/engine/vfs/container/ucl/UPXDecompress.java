/*
 * $Id: UPXDecompress.java,v 1.2 2005/09/03 16:16:40 kurti Exp $
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

import java.io.*;

/**
 * Decompresses upx compressed files
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class UPXDecompress {
    
    private final RandomAccessFile raf;

    private final long file_size;
    
    /** Creates a new instance of UPXDecompress */
    public UPXDecompress(RandomAccessFile raf, long file_size) {
        this.raf       = raf;
        this.file_size = file_size;
    }
    
    private static final int MAX_IC = 20;
    private static final String UPX_SECTION_NAME = "UPX";
    private static final int MAX_VERSION = 12;
    
    public static final int
            M_NRV2B_LE32 = 2,
            M_NRV2B_8    = 3,
            M_NRV2B_LE16 = 4,
            M_NRV2D_LE32 = 5,
            M_NRV2D_8    = 6,
            M_NRV2D_LE16 = 7;
    
    private boolean rtm;
    private long pe_offset;
    private PEHeader ih;
    private PESection[] isection;
    private final PackHeader ph = new PackHeader();
    
    public boolean canUnpack() throws IOException {
        if (!readFileHeader()) {
            return false;
        }
        
        int objects = ih.getObjects();
        if (objects != 3) {
            return false;
        }
        
        isection = new PESection[objects];
        raf.seek(pe_offset + PEHeader.SIZE);
        for (int i = 0; i < isection.length; i++) {
            final byte[] data = new byte[PESection.SIZE];
            raf.readFully(data);
            isection[i] = new PESection(data);
        }
        
        if (((ih.getDDirsSize(15) == 0)
              && !(ih.getEntry() > isection[1].getVAddress()))) {
            return false;
        }
        
        if (isection[0].getName().startsWith(UPX_SECTION_NAME)) {
            return (readPackHeader(1024, isection[1].getRawDataPointer() - 64)
                    || readPackHeader(1024, isection[2].getRawDataPointer()));
        }
        
        return false;
    }
    
    protected boolean readPackHeader(int len, long seek_offset)
                throws IOException {
        if (len <= 0 || seek_offset < 0) {
            return false;
        }
        
        final byte[] buf = new byte[len];
        raf.seek(seek_offset);
        raf.readFully(buf);
        if (!ph.fillPackHeader(buf)) {
            return false;
        }
        
        if (!ph.checkPackHeader(buf)) {
            return false;
        }
        
        if (ph.getVersion() > MAX_VERSION) {
            throw new IOException("need a newer version of UPX");
        }
        
        if (ph.getCLength() >= ph.getULength()
                || ph.getCLength() >= file_size) {
            throw new IOException("header corrupted");
        }
        
        if (ph.getMethod() < M_NRV2B_LE32 || ph.getMethod() > M_NRV2D_LE16) {
            throw new IOException("unknown compression method");
        }
        
        return true;
    }
    
    protected boolean readFileHeader() throws IOException {
        EXEHeader h;
        pe_offset = 0;
        int ic;
        for (ic = 0; ic < MAX_IC; ic++) {
            raf.seek(pe_offset);
            final byte[] data = new byte[EXEHeader.SIZE];
            raf.readFully(data);
            h = new EXEHeader(data);
            if (h.getMZ() == ('M' | ('Z' << 8))) { // DOS exe
                if (h.getRelocationOffset() >= 0x40) { // new format exe
                    pe_offset += h.getNextEPos();
                } else {
                    pe_offset += (h.getP512() << 9) + h.getM512()
                                 - (h.getM512() != 0 ? 512 : 0);
                }
            } else if (h.getLE32(0) == ('P' | ('E' << 8))) {
                break;
            } else {
                return false;
            }
        }
        if (ic == 20) {
            return false;
        }
        
        final byte[] data = new byte[PEHeader.SIZE];
        raf.seek(pe_offset);
        raf.readFully(data);
        ih = new PEHeader(data);
        
        final String sStub = "32STUB";
        final byte[] aStub = new byte[sStub.length()];
        raf.seek(0x200);
        raf.readFully(aStub);
        rtm = sStub.equals(new String(aStub));
        
        return true;
    }
    
    private int ilen;
    private byte[] src, dst;
    public void decompress(OutputStream os) throws IOException {
        if (ih == null) {
            throw new IllegalStateException("Need to call 'canUnpack()' first");
        }
        raf.seek(isection[1].getRawDataPointer() - 64 + ph.getBufferOffset()
                 + ph.getPackHeaderSize());
        src = new byte[ph.getCLength()];
        raf.readFully(src);
        dst = new byte[ph.getULength()];
        bitBuffer = 0;
        bitCount  = 0;
        ilen = 0;
        int olen = 0, last_m_off = 1;
        while (true) {
            while (getbit() != 0) {
                dst[olen++] = src[ilen++];
            }

            long m_off = 1;
            do {
                m_off = (m_off << 1) + getbit();
            } while (getbit() == 0);

            if (m_off == 2) {
                m_off = last_m_off;
            } else {
                m_off = ((m_off - 3) << 8) + CStructure.getByte(src, ilen++);
                if (m_off == 0xffffffffL) {
                    break;
                }
                last_m_off = (int) ++m_off;
            }

            int m_len = getbit();
            m_len = (m_len << 1) + getbit();
            if (m_len == 0) {
                m_len++;
                do {
                    m_len = (m_len << 1) + getbit();
                } while (getbit() == 0);
                m_len += 2;
            }
            m_len += (m_off > 0xd00) ? 1 : 0;
            int m_pos = (int) (olen - m_off);
            dst[olen++] = dst[m_pos++];
            do {
                dst[olen++] = dst[m_pos++];
            } while (--m_len > 0);
        }
        if (os != null) {
            os.write(dst);
        }
        
        if (ilen < src.length) {
            throw new IOException("UPX input not consumed");
        }
    }

    private long bitBuffer;
    private int  bitCount;
    protected int getbit() {
        if (bitCount > 0) {
            bitCount--;
            return (int) ((bitBuffer >> bitCount) & 1);
        }
        
        bitCount = 31;
        bitBuffer =    CStructure.getByte(src, ilen++)
                    + (CStructure.getByte(src, ilen++) <<  8)
                    + (CStructure.getByte(src, ilen++) << 16)
                    + (CStructure.getByte(src, ilen++) << 24);
        return (int) ((bitBuffer >> 31) & 1);
    }
    
    public void close() throws IOException {
        raf.close();
    }

    public boolean isRtm() {
        return rtm;
    }
}
