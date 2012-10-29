/*
 * $Id: MatchArrayCensor.java,v 1.6 2004/05/22 14:48:10 kurti Exp $
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
 * ***** END LICENSE BLOCK *****
 */ 
package org.openantivirus.engine.censor.matcharray;

import java.io.*;

import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.vfs.*;

/**
 * Pattern matching by array
 *
 * @author  Kurt Huwig
 * @version $Revision: 1.6 $
 */
public class MatchArrayCensor implements Censor {
    private static final int BUFFER_SIZE = 32768;

    private final PositionFoundListener[][] matchArray;
    private final PositionFoundEvent pfe = new PositionFoundEvent();

    private int match;
    
    public MatchArrayCensor(PositionFoundListener[][] matchArray) {
        this.matchArray = matchArray;
    }
    
    public int censor(VfsEntry entry) throws MalwareFoundException, ScanException {
        try {
            final InputStream is = new FileInputStream(entry.getFile());

            try {
                scanStream(entry, is);
            } finally {
                is.close();
            }
            
            return NOTHING_FOUND;
            
        } catch (IOException ioe) {
            throw new ScanException(ioe);
        }
    }

    protected void scanStream(VfsEntry entry, InputStream is)
    throws IOException, MalwareFoundException {
        byte[] abBuffer = new byte[3 * BUFFER_SIZE];
        
        int iLength = fillBuffer(is, abBuffer, 0, abBuffer.length);
        
        if (iLength == -1) {
            return;
        }
        
        match = abBuffer[0] & 0xff;
        
        if (iLength < abBuffer.length) {
            update(entry, 1L, abBuffer, 1, iLength - 1, 1, 0);
            
        } else  {
            update(entry,
                   1L,
                   abBuffer,
                   1,
                   2 * BUFFER_SIZE - 1,
                   1,
                   BUFFER_SIZE - 1);
            
            long fileOffset = 2 * BUFFER_SIZE;
            int iBufferPos = 2 * BUFFER_SIZE;
            int iReadAheadPos = 0;
            do {
                iLength = fillBuffer(is,
                                     abBuffer,
                                     iReadAheadPos,
                                     BUFFER_SIZE);
                
                update(entry,
                       fileOffset,
                       abBuffer,
                       iBufferPos,
                       BUFFER_SIZE,
                       BUFFER_SIZE,
                       iLength != -1 ? iLength : 0);
                
                iBufferPos = iReadAheadPos;
                iReadAheadPos += BUFFER_SIZE;
                if (iReadAheadPos >= 3 * BUFFER_SIZE) {
                    iReadAheadPos -= 3 * BUFFER_SIZE;
                }
                fileOffset += BUFFER_SIZE;
                
            } while (iLength == BUFFER_SIZE);
            
            if (iLength != -1) {
                update(entry,
                       fileOffset,
                       abBuffer,
                       iBufferPos,
                       iLength,
                       BUFFER_SIZE,
                       0);
            }
        }
    }
    
    /**
     * scans the next block of the stream for viruses; blocks are expected to
     * appear in the correct order
     */
    protected void update(VfsEntry entry,
                          long fileOffset,
                          byte[] ab,
                          int iOffset,
                          int iLength,
                          int iPrefix,
                          int iSuffix) throws MalwareFoundException {
        final int prefix = iOffset + iPrefix;
        final int scanLength = iOffset + iLength;
        final int postfix = scanLength + iSuffix;
        for (int i = iOffset; i < scanLength; i++) {
            match = ((match << 8) ^ ab[i]) & 0xffff;
           
            final PositionFoundListener[] matchListener = matchArray[match];
            if (matchListener != null) {
                final int iPosition = i - 1;
                pfe.setValues(
                        entry,
                        fileOffset + iPosition,
                        ab,
                        iPosition,
                        2,
                        iPosition - prefix,
                        postfix - iPosition);
                
                for (int j = 0; j < matchListener.length; j++) {
                    matchListener[j].positionFound(pfe);
                }
            }
        }
    }
    
    /**
     * fills the buffer up to the end unless the end of data is reached, or
     * an IOException occurs
     */
    protected int fillBuffer(InputStream stream,
                             byte[] buffer,
                             int offset,
                             int length) throws IOException {
        int iRead = 0;
        do {
            int iLength = stream.read(buffer, offset + iRead, length - iRead);
            if (iLength == -1) {
                return iRead == 0 ? -1 : iRead;
            }
            iRead += iLength;
        } while (iRead < length);
        return iRead;
    }
}
