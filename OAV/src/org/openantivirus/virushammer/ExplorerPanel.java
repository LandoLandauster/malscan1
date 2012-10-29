/*
 * $Id: ExplorerPanel.java,v 1.4 2005/03/03 12:18:06 kurti Exp $
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
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * ExplorerPanel
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public class ExplorerPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public static final String VERSION =
        "$Id: ExplorerPanel.java,v 1.4 2005/03/03 12:18:06 kurti Exp $";
    
    /** Table for the scantargets */
    private final JTable jtFiles = new JTable();
    
    /** Column names of the scantargets */
    private static final String[] COLUMN_NAMES = {
        "Target_name", "Subfolders"
    };
    
    /** Actions */
    private final AddTargetAction addTargetAction = new AddTargetAction();
    private final RemoveTargetAction removeTargetAction =
            new RemoveTargetAction();
    
    /** Listener */
    private final ListSelectionListener scanTargetListListener =
            new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent lse) {
            removeTargetAction.setScanTarget((ScanTarget) scanTargetList
                    .getElementAt(jtFiles.getSelectedRow()));
        }
    };
    
    /** Holds value of property scanTargetList. */
    private ScanTargetList scanTargetList;
    
    public ExplorerPanel() {
        super(new BorderLayout());
        
        createTargetTable();
        createActionPanel();
        registerListener();
    }
    
    /** creates the table for the scantargets */
    protected void createTargetTable() {
        jtFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        add(new JScrollPane(jtFiles), BorderLayout.CENTER);
    }
    
    /** creates the panel with the actions */
    protected void createActionPanel() {
        final JPanel jp = new JPanel();
        
        JButton jb = new JButton(addTargetAction);
        jb.setMnemonic(L10N.getMnemonic("Add"));
        jp.add(jb);
        
        jb = new JButton(removeTargetAction);
        jb.setMnemonic(L10N.getMnemonic("Remove"));
        jp.add(jb);
        add(jp, BorderLayout.SOUTH);
    }

    /** registers the local listeners */
    protected void registerListener() {
        jtFiles.getSelectionModel().addListSelectionListener(
                scanTargetListListener);
    }
    
    /** Setter for property scanTargetList.
     * @param scanTargetList New value of property scanTargetList.
     */
    public void setScanTargetList(ScanTargetList newScanTargetList) {
        scanTargetList = newScanTargetList;
        
        final AbstractTableModel model = new AbstractTableModel() {
            private static final long serialVersionUID = 1L;
            
            public int getRowCount() {
                return scanTargetList.getSize();
            }
            
            public int getColumnCount() {
                return COLUMN_NAMES.length;
            }
            
            public String getColumnName(int column) {
                return L10N.getString(COLUMN_NAMES[column]);
            }
            
            public Object getValueAt(int row, int column) {
                final ScanTarget st = (ScanTarget) scanTargetList.getElementAt(
                        row);
                switch (column) {
                case 0:
                    return st.getPath();
                case 1:
                    return st.getSubfolders() ? L10N.getString("Yes")
                                              : L10N.getString("No");
                }
                return null;
            }
            
        };
        
        scanTargetList.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent lde) {
                model.fireTableRowsInserted(lde.getIndex0(), lde.getIndex1());
            }
            
            public void intervalRemoved(ListDataEvent lde) {
                jtFiles.getSelectionModel().setSelectionInterval(-1, -1);
                model.fireTableRowsDeleted(lde.getIndex0(), lde.getIndex1());
            }
            
            public void contentsChanged(ListDataEvent lde) {
                model.fireTableRowsUpdated(lde.getIndex0(), lde.getIndex1());
            }
        });
        jtFiles.setModel(model);
        
        final TableColumnModel tcm = jtFiles.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(300);
        tcm.getColumn(1).setPreferredWidth(100);
        
        addTargetAction.setScanTargetList(scanTargetList);
        removeTargetAction.setScanTargetList(scanTargetList);
    }
}
