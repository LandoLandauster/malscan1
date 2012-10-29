/*
 * $Id: ScannerOutputPanel.java,v 1.7 2005/03/03 12:18:06 kurti Exp $
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

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.vfs.*;

/**
 * ScannerOutputPanel
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.7 $
 */
public class ScannerOutputPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public static final String VERSION =
        "$Id: ScannerOutputPanel.java,v 1.7 2005/03/03 12:18:06 kurti Exp $";
    
    private final DefaultTableModel outputModel = new DefaultTableModel(
            new String[] {
                L10N.getString("Filename"),
                L10N.getString("Contains")
            }, 0);
    
    private final JTable outputTable = new JTable(outputModel);
    
    public ScannerOutputPanel() {
        super(new BorderLayout());
        final JScrollPane jsp = new JScrollPane(outputTable);
        add(jsp, BorderLayout.CENTER);
        outputTable.setShowGrid(false);
    }
    
    public void addFoundVirus(MalwareFoundException mfe) {
        outputModel.addRow(new String[] {mfe.getEntry().getName(),
                                         mfe.getName()});
    }
    
    public void addException(VfsEntry entry, Exception exception) {
        outputModel.addRow(new String[] {entry.getName(),
                                         exception.getClass().getName() + ": "
                                         + exception.getMessage()});
    }
    
    public void clearList() {
        outputModel.setRowCount(0);
    }
}
