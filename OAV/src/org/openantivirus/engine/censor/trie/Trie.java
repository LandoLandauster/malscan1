/*
 * $Id: Trie.java,v 1.4 2004/05/20 13:43:16 kurti Exp $
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
 * Trie
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public class Trie implements StringSearch {
    public final static int MINIMUM_LENGTH = 3;
    
    private TrieNode nRoot = new TrieNode();
    
    private int[] nodeDepthCount = new int[MINIMUM_LENGTH];
    
    public void addString(byte[] abPattern, PositionFoundListener pfl) {
        if (abPattern.length < MINIMUM_LENGTH) {
            throw new IllegalArgumentException("String too short");
        }
        
        // start at rootnode
        TrieNode nPos = nRoot;
        
        // add nodes into the tree for the prefix with length MINIMUM_LENGTH
        for (int i = 0; i < MINIMUM_LENGTH; i++) {
            int iCharacter = abPattern[i] & 0xff;
            
            TrieNode next = nPos.isLastNode() ? null
                                              : nPos.getTrans(iCharacter);
            if (next == null) {
                next = new TrieNode();
                nPos.setTrans(iCharacter, next);
                nodeDepthCount[i]++;
            }
            
            nPos = next;
        }
        nPos.addPositionFoundListener(pfl);
    }
    
    public TrieNode getRootNode() {
        return nRoot;
    }
    
    /**
     * Prepares the Trie for usage. This method has to be called before the
     * trie can be used
     */
    public void prepare() {
        // initialize the root node
        final LinkedList children = new LinkedList();
        nRoot.setFailure(null); // null = top node; to end following
        for (int i = 0; i < TrieNode.NUM_CHILDS; i++) {
            final TrieNode child = nRoot.getTrans(i);
            if (child == null) {
                nRoot.setTrans(i, nRoot);
            } else {
                child.setFailure(nRoot);
                children.addLast(child);
            }
        }
        
        while (!children.isEmpty()) {
            final TrieNode node = (TrieNode) children.removeFirst();
            if (node.isLastNode()) {
                continue;
            }
            for (int i = 0; i < TrieNode.NUM_CHILDS; i++) {
                final TrieNode child = node.getTrans(i);
                if (child == null) {
                    node.setTrans(i, node.getFailure().getTrans(i));
                } else {
                    child.setFailure(node.getFailure().getTrans(i));
                    children.addLast(child);
                }
            }
        }
    }
    
    public int[] getNodeDepths() {
        return nodeDepthCount;
    }

    public Censor createCensor() {
        return new TrieCensor(this);
    }
}
