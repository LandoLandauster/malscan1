/*
 * $Id: PositionFoundEvent.java,v 1.2 2004/05/19 08:09:49 kurti Exp $
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

package org.openantivirus.engine.censor;

import org.openantivirus.engine.vfs.*;

/**
 * Indication the position within the file where something has been found
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class PositionFoundEvent {
    public byte[] buffer;
    public VfsEntry entry;
    public long fileOffset;
    public int bufferOffset, length, prefix, suffix;

    public PositionFoundEvent() {
        // nothing
    }
    
    /**
     * @param entry the entry containing the position
     * @param fileOffset offset within the file
     * @param buffer the (ring) buffer containing the string found
     * @param bufferOffset offset of the start of the string within the buffer
     * @param length length of the string found
     * @param prefixLength number of valid bytes before the offset byte
     * @param suffixLength number of valid bytes after the offset byte
     */
    public PositionFoundEvent(VfsEntry entry,
                              long fileOffset,
                              byte[] buffer, int bufferOffset, int length,
                              int prefix, int suffix) {
        setValues(entry, fileOffset, buffer, bufferOffset, length, prefix, 
                  suffix);
    }
    
    /**
     * @param entry the entry containing the position
     * @param fileOffset offset within the file
     * @param buffer the (ring) buffer containing the string found
     * @param bufferOffset offset of the start of the string within the buffer
     * @param length length of the string found
     * @param prefixLength number of valid bytes before the offset byte
     * @param suffixLength number of valid bytes after the offset byte
     */
    public void setValues(VfsEntry entry,
                          long fileOffset,
                          byte[] buffer, int bufferOffset, int length,
                          int prefix, int suffix) {
        this.entry        = entry;
        this.fileOffset   = fileOffset;
        this.buffer       = buffer;
        this.bufferOffset = bufferOffset;
        this.length       = length;
        this.prefix       = prefix;
        this.suffix       = suffix;
    }
}