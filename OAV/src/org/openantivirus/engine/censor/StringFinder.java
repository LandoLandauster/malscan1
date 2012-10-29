/*
 * $Id: StringFinder.java,v 1.3 2004/05/22 12:22:27 kurti Exp $
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
package org.openantivirus.engine.censor;

import java.util.*;

import org.openantivirus.engine.credo.*;

/**
 * Can add strings to the stringsearch and adds itself as a listener to it
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class StringFinder {
    private final StringSearch stringSearch;
    private int multipartCount = 0;
    
    public StringFinder(StringSearch stringSearch) {
        this.stringSearch = stringSearch;
    }
    
    public void addString(String sPattern,
                          final int offset,
                          final PositionFoundListener pfl) {
        final byte[] pattern = new WildcardPattern(sPattern).pattern;
        final byte[] searchPattern = new byte[pattern.length - offset];
        System.arraycopy(pattern, offset,
                         searchPattern, 0, searchPattern.length);
        stringSearch.addString(searchPattern, new PositionFoundListener() {
            public void positionFound(PositionFoundEvent pfe)
            throws MalwareFoundException {
                if (checkString(pattern, offset, pfe)) {
                    adjustPosition(offset, pattern, pfe);
                    pfl.positionFound(pfe);
                }
            }

        });
    }

    public void addWildcardString(final WildcardPattern wp,
                                  final int offset,
                                  final PositionFoundListener pfl) {
        final byte[] searchPattern = new byte[wp.pattern.length - offset];
        System.arraycopy(wp.pattern, offset,
                         searchPattern, 0, searchPattern.length);
        stringSearch.addString(searchPattern, new PositionFoundListener() {
            public void positionFound(PositionFoundEvent pfe)
            throws MalwareFoundException {
                if (checkWildcardString(wp, offset, pfe)) {
                    adjustPosition(offset, wp.pattern, pfe);
                    pfl.positionFound(pfe);
                }
            }
        });
    }
    
    public void addMultipartString(String pattern,
                                   String offsets,
                                   final PositionFoundListener pfl) {
        final StringTokenizer stOffsets = new StringTokenizer(offsets, "*");
        final int partCount = stOffsets.countTokens();
        final int multipartId = multipartCount++;
        
        int part = 1;
        for (StringTokenizer stPatterns = new StringTokenizer(pattern, "*");
             stPatterns.hasMoreTokens();
             part++) {
            
            final WildcardPattern subPattern =
                new WildcardPattern(stPatterns.nextToken());
            final int subOffset = Integer.parseInt(stOffsets.nextToken());
            
            final byte[] searchPattern =
                new byte[subPattern.pattern.length - subOffset];
            System.arraycopy(subPattern.pattern, subOffset,
                             searchPattern, 0, searchPattern.length);
            
            final int partId = part;
            stringSearch.addString(searchPattern, new PositionFoundListener() {
                public void positionFound(PositionFoundEvent pfe)
                throws MalwareFoundException {
                    int[] foundParts = pfe.entry.foundParts;
                    if (foundParts == null) {
                        foundParts = new int[multipartCount];
                        pfe.entry.foundParts = foundParts;
                    }
                    
                    final int foundPart = foundParts[multipartId];
                    if (foundPart != partId - 1) {
                        return;
                    }
                    
                    if (checkWildcardString(subPattern, subOffset, pfe)) {
                        if (partId == partCount) {
                            pfl.positionFound(new PositionFoundEvent(
                                    pfe.entry,
                                    -1,
                                    null,
                                    -1,
                                    -1,
                                    -1,
                                    -1));
                        } else {
                            pfe.entry.foundParts[multipartId] = partId;
                        }
                    }
                }
            });
        }
        
    }
    
    protected static void adjustPosition(int offset,
                                         byte[] pattern,
                                         PositionFoundEvent pfe) {
        pfe.bufferOffset -= offset;
        pfe.fileOffset -= offset;
        pfe.length = pattern.length;
        pfe.prefix -= offset;
        pfe.suffix += pattern.length - offset - 2;
    }
    
    public static boolean checkString(byte[] pattern,
                               int offset,
                               PositionFoundEvent pfe) {
        if (pfe.fileOffset < offset) {
            return false;
        }
        
        int iBufferPos  = pfe.bufferOffset - offset;
        if (iBufferPos < 0) {
            iBufferPos += pfe.buffer.length;
        }
        
        final int iSuffixEnd = pfe.bufferOffset + pfe.suffix;

        for (int i = 0; i < pattern.length; i++) {
            if (iBufferPos == iSuffixEnd) {
                return false;
            }
            
            if (pattern[i] != pfe.buffer[iBufferPos]) {
                return false;
            }
            
            iBufferPos++;
            if (iBufferPos == pfe.buffer.length) {
                iBufferPos -= pfe.buffer.length;
            }
        }
        
        return true;
    }

    
    public static boolean checkWildcardString(WildcardPattern wp,
                                              int offset,
                                              PositionFoundEvent pfe) {
        if (pfe.fileOffset < offset) {
            return false;
        }
        
        int iBufferPos  = pfe.bufferOffset - offset;
        if (iBufferPos < 0) {
            iBufferPos += pfe.buffer.length;
        }
        
        final int iSuffixEnd = pfe.bufferOffset + pfe.suffix;

        int skipIndex = 0;
        int skipCount = wp.skipList[0];
        for (int i = 0; i < wp.pattern.length; i++) {
            if (iBufferPos == iSuffixEnd) {
                return false;
            }
            
            if (skipCount == 0) {
                skipCount = wp.skipList[++skipIndex];
                i += skipCount;
                iBufferPos += skipCount;
                if (iBufferPos >= pfe.buffer.length) {
                    iBufferPos -= pfe.buffer.length;
                }
                skipCount = wp.skipList[++skipIndex];
            }
            
            skipCount--;
            
            if (wp.pattern[i] != pfe.buffer[iBufferPos]) {
                return false;
            }
            
            iBufferPos++;
            if (iBufferPos == pfe.buffer.length) {
                iBufferPos -= pfe.buffer.length;
            }
        }
        
        return true;
    }
    
}
