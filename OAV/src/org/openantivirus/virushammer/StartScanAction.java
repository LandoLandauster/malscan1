/*
 * $Id: StartScanAction.java,v 1.6 2005/03/03 12:18:06 kurti Exp $
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

import javax.swing.event.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.vfs.*;

/**
 * StartScanAction
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.6 $
 */
public class StartScanAction extends PerformableAction {
    private static final long serialVersionUID = 1L;
    
    /** Holds value of property scanTargetList. */
    private ScanTargetList scanTargetList = null;
    
    /** Listener on items being added or removed */
    private ListDataListener scanTargetListener = new ListDataListener() {
        public void intervalAdded(ListDataEvent lde) {
            updateEnabled();
        }
        
        public void intervalRemoved(ListDataEvent lde) {
            updateEnabled();
        }
        
        public void contentsChanged(ListDataEvent lde) {
            updateEnabled();
        }
    };
    
    private ScanListener scanListener = new ScanListener() {
        public void startingScan() {
            updateEnabled();
        }
        
        public void finishedScan() {
            updateEnabled();
        }
        
        public void exceptionThrown(Exception exception) {}

        public void scanning(VfsEntry vfsEntry) throws ScanAbortedException {}

        public void malwareFound(MalwareFoundException malwareFoundException) {}        
    };
    
    /** Holds value of property scannerThread. */
    private ScannerThread scannerThread;
    
    public StartScanAction() {
        super(L10N.getString("Start_scanning"));
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        scannerThread.startScanning();
    }    
    
    /** @returns if this action should be enabled  */
    public boolean isPerformable() {
        return    (scanTargetList != null)
               && (scannerThread  != null)
               && (!scannerThread.isScanning())
               && (scanTargetList.getSize() > 0);
    }
    
    /** Setter for property scanTargetList.
     * @param scanTargetList New value of property scanTargetList.
     */
    public void setScanTargetList(ScanTargetList scanTargetList) {
        if (scanTargetList != null) {
            scanTargetList.removeListDataListener(scanTargetListener);
        }
        this.scanTargetList = scanTargetList;
        if (scanTargetList != null) {
            scanTargetList.addListDataListener(scanTargetListener);
        }
        updateEnabled();
    }
    
    /** Setter for property scannerThread.
     * @param scannerThread New value of property scannerThread.
     */
    public void setScannerThread(ScannerThread scannerThread) {
        if (this.scannerThread != null) {
            this.scannerThread.removeScanListener(scanListener);
        }
        this.scannerThread = scannerThread;
        if (scannerThread != null) {
            scannerThread.addScanListener(scanListener);
        }
        updateEnabled();
    }
}
