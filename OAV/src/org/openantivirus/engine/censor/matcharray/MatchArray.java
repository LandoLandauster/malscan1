/*
 * $Id: MatchArray.java,v 1.4 2004/05/22 14:48:10 kurti Exp $
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

import java.util.*;

import org.openantivirus.engine.censor.*;

/**
 * Pattern matching by array
 *
 * @author  Kurt Huwig
 * @version $Revision: 1.4 $
 */
public class MatchArray implements StringSearch {
    public final static int MINIMUM_LENGTH = 2;
    
    private Collection[] colMatches = new Collection[1 << 16];
    private PositionFoundListener[][] matches =
        new PositionFoundListener[1 << 16][];
    
    public void addString(byte[] abPattern, PositionFoundListener pfl) {
        if (abPattern.length < MINIMUM_LENGTH) {
            throw new IllegalArgumentException("String too short");
        }
        
        final int match = ((abPattern[0] << 8) ^ abPattern[1]) & 0xffff;
        
        Collection colMatch = colMatches[match];
        if (colMatch == null) {
            colMatch = new LinkedList();
            colMatches[match] = colMatch;
        }
        
        colMatch.add(pfl);
    }
    
    public Censor createCensor() {
        return new MatchArrayCensor(matches);
    }
    
    public void prepare() {
        for (int i = 0; i < colMatches.length; i++) {
            final Collection matchListener = colMatches[i];
            if (matchListener != null) {
                final PositionFoundListener[] matchList =
                    new PositionFoundListener[matchListener.size()];
                matches[i] = matchList;
                
                int j = 0;
                for (Iterator it = matchListener.iterator(); it.hasNext();) {
                    matchList[j++] = (PositionFoundListener) it.next();
                }
                colMatches[i] = null;
            }
        }
    }
    
}
