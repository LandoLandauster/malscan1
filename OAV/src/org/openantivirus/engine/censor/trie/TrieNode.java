/*
 * $Id: TrieNode.java,v 1.2 2004/05/19 07:33:39 kurti Exp $
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

import java.util.*;

import org.openantivirus.engine.censor.*;

/**
 * A node in the trie
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class TrieNode {
    /**
     * Number of children a node has; currently one for every possible byte
     * value
     */
    public final static int NUM_CHILDS = 256;
    
    private TrieNode nFailure = null;
    private static int iInstances = 0;
    private boolean bIsLastNode = true;
    private Collection colPositionFoundListener = null;
    
    private TrieNode[] anTrans = null;
    
    public TrieNode() {
        iInstances++;
    }
    
    public void setTrans(int iByte, TrieNode n) {
        if (anTrans == null) {
            anTrans = new TrieNode[NUM_CHILDS];
            bIsLastNode = false;
        }
        anTrans[iByte] = n;
    }
    
    public TrieNode getTrans(int index) {
        return anTrans[index];
    }
    
    public boolean isLastNode() {
        return bIsLastNode;
    }
    
    public void addPositionFoundListener(PositionFoundListener pfl) {
        if (colPositionFoundListener == null) {
            colPositionFoundListener = new LinkedList();
        }
        colPositionFoundListener.add(pfl);
    }
    
    public Collection getStringSearchListener() {
        return colPositionFoundListener;
    }
    
    public void setFailure(TrieNode nFailure) {
        this.nFailure = nFailure;
    }
    
    public TrieNode getFailure() {
        return nFailure;
    }

    public static int getInstanceCount() {
        return iInstances;
    }
}
