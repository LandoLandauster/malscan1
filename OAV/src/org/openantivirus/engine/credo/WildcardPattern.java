/*
 * $Id: WildcardPattern.java,v 1.3 2004/09/19 14:01:37 kurti Exp $
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
 
package org.openantivirus.engine.credo;

import java.util.*;

/**
 * Contains information about patterns with '?'-wildcards
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class WildcardPattern {
    public final byte[] pattern;
    public final int[] skipList;
    
    public WildcardPattern(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Malformed hexstring: " + hex);
        }
        
        pattern = new byte[hex.length() / 2];
        
        final Collection colSkipList = new LinkedList();
        
        int skipCount = 0;
        boolean skip = false;
        for (int i = 0; i < hex.length(); i += 2) {
            final char c1 = hex.charAt(i);
            final char c2 = hex.charAt(i + 1);
            if (c1 == '?' && c2 == '?') {
                if (skip) {
                    skipCount++;
                } else {
                    skip = true;
                    colSkipList.add(new Integer(skipCount));
                    skipCount = 1;
                }
            } else {
                if (!skip) {
                    skipCount++;
                } else {
                    skip = false;
                    colSkipList.add(new Integer(skipCount));
                    skipCount = 1;
                }
                pattern[i / 2] = (byte) ((Character.digit(c1, 16) << 4)
                                         + Character.digit(c2, 16));
            }
        }
        
        if (skipCount > 0) {
            colSkipList.add(new Integer(skipCount));
        }
        
        skipList = new int[colSkipList.size()];
        final Iterator it = colSkipList.iterator();
        for (int i = 0; i < skipList.length; i++) {
            skipList[i] = ((Integer) it.next()).intValue();
        }
    }
}
