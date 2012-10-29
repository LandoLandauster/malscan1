/*
 * $Id: VirusHammer.java,v 1.12 2004/05/26 20:19:52 kurti Exp $
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
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.credo.*;
import org.openantivirus.engine.vfs.*;

/**
 * L10N
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.12 $
 */
public class VirusHammer {
    
    /** Actions */
    private final Action
            patternFindAction = new PatternFindAction(),
            preferencesAction = new PreferencesAction();
    
    private final StartScanAction startScanAction = new StartScanAction();
    private final StopScanAction  stopScanAction  = new StopScanAction();
    private final ExitAction      exitAction      = new ExitAction();
    
    /** List of all the targets to scan */
    private final ScanTargetList scanTargetList = new ScanTargetList();
    
    private final JFrame frame = new JFrame();
    private final WriteableScanConfiguration scanConf;
    private final ScannerThread scannerThread;
    private final StatusModel statusModel = new StatusModel();
    private final ScannerOutputPanel scannerOutputPanel =
            new ScannerOutputPanel();
    
    /** Listener */
    private final ScanListener scanListener = new ScanListener() {
        private VfsEntry scanEntry = null;
        
        public void startingScan() {
            scannerOutputPanel.clearList();
        }

        public void scanning(VfsEntry vfsEntry) throws ScanAbortedException {
            statusModel.setText(MessageFormat.format(
                    L10N.getString("Scanning_file"),
                    new Object[] {vfsEntry.getName()}));
        }
        
        public void malwareFound(MalwareFoundException malwareFoundException) {
            scannerOutputPanel.addFoundVirus(malwareFoundException);
        }
        
        public void exceptionThrown(Exception exception) {
            scannerOutputPanel.addException(scanEntry, exception);
        }
        
        public void finishedScan() {
            scanEntry = null;
            statusModel.setText(L10N.getString("Idle"));
        }
    };
    
    public VirusHammer() throws CredoException, IOException {
        scanConf = new WriteableScanConfiguration(
                new DefaultScanConfiguration());
        scanConf.putBoolean("engine.halt-on-malware-found", false);
        scannerThread = new ScannerThread(scanConf);
        initScanTargetList();
        
        frame.setTitle(L10N.getString("VirusHammer") + " "
                       + L10N.getString("VirusHammerVersion"));
        
        frame.setJMenuBar(createMenuBar());
        
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (exitAction.isEnabled()) {
                    exitAction.actionPerformed(new ActionEvent(we.getSource(),
                            we.getID(), null));
                }
            }
        });
        
        createComponents();
        initActions();
        
        frame.setSize(640, 480);
        frame.setVisible(true);
        statusModel.setText(L10N.getString("Idle"));
    }
    
    protected JMenuBar createMenuBar() {
        final JMenuBar jmb = new JMenuBar();
        
        JMenu jm;
        
        // File menu
        jm = new JMenu(L10N.getString("File"));
        jm.setMnemonic(L10N.getMnemonic("File"));
        jm.add(preferencesAction).setMnemonic(L10N.getMnemonic("Preferences"));
        jm.addSeparator();
        jm.add(exitAction).setMnemonic(L10N.getMnemonic("Exit"));
        jmb.add(jm);
        
        // Tools menu
        jm = new JMenu(L10N.getString("Tools"));
        jm.setMnemonic(L10N.getMnemonic("Tools"));
        jm.add(patternFindAction).setMnemonic(L10N.getMnemonic("Find_pattern"));
        jmb.add(jm);
        
        return jmb;
    }
    
    protected void createComponents() {
        // JPanel for the center of the frame, so that the toolbar can be
        // moved
        final JPanel jpCenter  = new JPanel(new BorderLayout());
        final JPanel jpExpl    = new JPanel(new BorderLayout());
        final JPanel jpTargets = new JPanel(new BorderLayout());
        final JPanel jpEast    = new JPanel(new GridBagLayout());
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                jpExpl, scannerOutputPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(300);
        jpCenter.add(splitPane,         BorderLayout.CENTER);
        jpCenter.add(createStatusBar(), BorderLayout.SOUTH);
        frame.getContentPane().add(jpCenter, BorderLayout.CENTER);
        jpExpl.add(jpTargets, BorderLayout.CENTER);
        jpExpl.add(jpEast,    BorderLayout.EAST);
        
        jpTargets.setBorder(BorderFactory.createTitledBorder(
                L10N.getString("Scan_targets")));
        final ExplorerPanel explorerPanel = new ExplorerPanel();
        explorerPanel.setScanTargetList(scanTargetList);
        jpTargets.add(explorerPanel, BorderLayout.CENTER);
        
        final GridBagConstraints gbcEastButton = new GridBagConstraints();
        gbcEastButton.gridwidth = GridBagConstraints.REMAINDER;
        gbcEastButton.insets    = new Insets(0, 0, 5, 0);
        gbcEastButton.fill      = GridBagConstraints.HORIZONTAL;
        
        JButton jb = new JButton(startScanAction);
        jb.setMnemonic(L10N.getMnemonic("Start_scanning"));
        jpEast.add(jb, gbcEastButton);
        
        jb = new JButton(stopScanAction);
        jb.setMnemonic(L10N.getMnemonic("Start_scanning"));
        jpEast.add(jb, gbcEastButton);
    }
    
    protected JPanel createStatusBar() {
        final JPanel statusBar = new JPanel(new BorderLayout());
        final JLabel status = new JLabel();
        status.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(status, BorderLayout.CENTER);
        statusModel.addPropertyChangeListener("text",
                                              new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                status.setText(statusModel.getText());
            }
        });
        return statusBar;
    }
    
    protected void initActions() {
        new Thread(scannerThread).start();
        scannerThread.setScanTargetList(scanTargetList);
        scannerThread.addScanListener(scanListener);
        startScanAction.setScanTargetList(scanTargetList);
        startScanAction.setScannerThread(scannerThread);
        stopScanAction.setScannerThread(scannerThread);
        exitAction.setScanTargetList(scanTargetList);
    }
    
    /**
     * initializes the list of ScanTargets with the settings from the
     * properties
     */
    protected void initScanTargetList() throws IOException {
        final Properties appProperties = new Properties();
        final File fileProperties = new File(System.getProperty("user.home")
                + File.separatorChar + "VirusHammer.properties");
        if (fileProperties.exists()) {
            final FileInputStream fis = new FileInputStream(fileProperties);
            try {
                appProperties.load(fis);
            } finally {
                fis.close();
            }
            final String sTargetList = appProperties.getProperty(
                    "scantargets");
            if (sTargetList != null) {
                final StringTokenizer st = new StringTokenizer(sTargetList,
                        String.valueOf(File.pathSeparatorChar));
                while (st.hasMoreTokens()) {
                    final File target = new File(st.nextToken());
                    scanTargetList.addScanTarget(new ScanTarget(
                            target, target.isDirectory()));
                }
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            L10N.setResourceBundle(ResourceBundle.getBundle(
                    "org/openantivirus/virushammer/VirusHammer"));
            new VirusHammer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
