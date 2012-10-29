/*
 * $Id: RemoveTargetAction.java,v 1.5 2005/03/03 12:18:06 kurti Exp $
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

import java.awt.event.ActionEvent;

/**
 * RemoveTargetAction
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Id: RemoveTargetAction.java,v 1.5 2005/03/03 12:18:06 kurti Exp $
 */
public class RemoveTargetAction extends PerformableAction {
    private static final long serialVersionUID = 1L;
    
    /** Holds value of property scanTarget. */
    private ScanTarget scanTarget = null;
    
    /** Holds value of property scanTargetList. */
    private ScanTargetList scanTargetList;
    
    public RemoveTargetAction() {
        super(L10N.getString("Remove"));
    }
    
    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        scanTargetList.removeScanTarget(scanTarget);
        setScanTarget(null);
    }    
    
    /** @returns if this action should be enabled  */
    public boolean isPerformable() {
        return    (scanTarget     != null)
               && (scanTargetList != null);
    }
    
    /** Setter for property scanTarget.
     * @param scanTarget New value of property scanTarget.
     */
    public void setScanTarget(ScanTarget scanTarget) {
        this.scanTarget = scanTarget;
        updateEnabled();
    }
    
    /** Setter for property scanTargetList.
     * @param scanTargetList New value of property scanTargetList.
     */
    public void setScanTargetList(ScanTargetList scanTargetList) {
        this.scanTargetList = scanTargetList;
        updateEnabled();
    }
    
}
