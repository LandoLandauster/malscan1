/*
 * $Id: ScanTargetList.java,v 1.4 2005/03/03 12:18:06 kurti Exp $
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

package org.openantivirus.virushammer;

import java.util.*;

/**
 * ScanTargetList
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public class ScanTargetList extends javax.swing.AbstractListModel  {
    private static final long serialVersionUID = 1L;
    
    public static final String VERSION =
        "$Id: ScanTargetList.java,v 1.4 2005/03/03 12:18:06 kurti Exp $";
    
    private final List targetList = new ArrayList();
    
    public void addScanTarget(ScanTarget target) {
        final int insertIndex = targetList.size();
        
        targetList.add(target);
        fireIntervalAdded(this, insertIndex, insertIndex + 1);
    }
    
    public void removeScanTarget(int removeIndex) {
        targetList.remove(removeIndex);
        fireIntervalRemoved(this, removeIndex, removeIndex);
    }
    
    public void removeScanTarget(ScanTarget scanTarget) {
        removeScanTarget(targetList.indexOf(scanTarget));
    }
    
    public int getSize() {
        return targetList.size();
    }
    
    public Object getElementAt(int index) {
        return index >= 0 ? targetList.get(index) : null;
    }
}
