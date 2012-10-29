/*
 * $Id: AddTargetAction.java,v 1.6 2005/09/03 16:16:40 kurti Exp $
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

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * Opens a filedialog to add a target to the targetlist, the selected file
 * (if any) is fired via a PropertyChangeEvent.
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.6 $
 */
public class AddTargetAction extends PerformableAction {
    private static final long serialVersionUID = 1L;
    
    /** Holds value of property scanTargetList. */
    private ScanTargetList scanTargetList;
    
    public AddTargetAction() {
        super(L10N.getString("Add") + "...");
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setApproveButtonText(L10N.getString("Add"));
        fc.setApproveButtonMnemonic(L10N.getMnemonic("Add"));
        fc.setDialogTitle(L10N.getString("Add_scan_target"));
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.exists()) {
                file = file.getParentFile();
            }
            scanTargetList.addScanTarget(new ScanTarget(file,
                                                        file.isDirectory()));
        }
    }
    
    /** @returns if this action should be enabled  */
    public boolean isPerformable() {
        return scanTargetList != null;
    }
    
    /** Setter for property scanTargetList.
     * @param scanTargetList New value of property scanTargetList.
     */
    public void setScanTargetList(ScanTargetList scanTargetList) {
        this.scanTargetList = scanTargetList;
        updateEnabled();
    }
    
}
