/*
 * $Id: TrieCensor.java,v 1.3 2004/05/20 13:34:33 kurti Exp $
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

package org.openantivirus.engine.censor.trie;

import java.io.*;
import java.util.*;

import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.vfs.*;

/**
 * Uses a Trie to scan the file; this class is not thread-safe!
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class TrieCensor implements Censor {
    private static final int BUFFER_SIZE = 32768;

    private TrieNode nRoot, nCurrent;
    
    public TrieCensor(Trie trie) {
        nRoot = trie.getRootNode();
    }

    public int censor(VfsEntry entry)
    throws MalwareFoundException, ScanException {
        nCurrent = nRoot;
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
            
        } else if (iLength < abBuffer.length) {
            update(entry, 0L, abBuffer, 0, iLength, 0, 0);
            
        } else  {
            update(entry, 0L, abBuffer, 0, 2 * BUFFER_SIZE, 0, BUFFER_SIZE);
            
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
     * scans the next block of the stream for virii; blocks are expected to
     * appear in the correct order
     */
    protected void update(VfsEntry entry,
                          long fileOffset,
                          byte[] ab,
                          int iOffset,
                          int iLength,
                          int iPrefix,
                          int iSuffix) throws MalwareFoundException {
        for (int i = iOffset; i < iOffset + iLength; i++) {
            nCurrent = nCurrent.getTrans(ab[ i ] & 0xff);
            if (nCurrent.isLastNode()) {
                final int iPosition = i - Trie.MINIMUM_LENGTH + 1;
                final PositionFoundEvent pfe = new PositionFoundEvent(
                        entry,
                        fileOffset + iPosition,
                        ab,
                        iPosition,
                        Trie.MINIMUM_LENGTH,
                        iPosition - iOffset + iPrefix,
                        iOffset + iLength - iPosition + iSuffix);
                
                for (Iterator it =
                        nCurrent.getStringSearchListener().iterator();
                     it.hasNext(); ) {
                    ((PositionFoundListener) it.next()).positionFound(pfe);
                }
                nCurrent = nCurrent.getFailure();
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
