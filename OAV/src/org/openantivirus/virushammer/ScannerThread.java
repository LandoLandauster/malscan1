/*
 * $Id: ScannerThread.java,v 1.8 2005/09/03 16:16:40 kurti Exp $
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

import java.io.*;
import java.util.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.credo.*;
import org.openantivirus.engine.vfs.*;

/**
 * Thread that scans the files, so that the GUI does not block
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.8 $
 */
public class ScannerThread implements Runnable {
    
    /** Utility field holding list of ScannerListeners. */
    private transient ArrayList scanListenerList;
    
    /** Holds value of property scanTargetList. */
    private ScanTargetList scanTargetList;
    
    /** Holds value of property scanning. */
    private volatile boolean scanning;
    
    /** If the Thread should abort scanning */
    private volatile boolean abortRequested;
    
    /** Listeners */
    private final ScanListener scanListener =
            new ScanListener() {
        public void startingScan() {}
        
        public void scanning(VfsEntry vfsEntry) throws ScanAbortedException {
            if (abortRequested) {
                throw new ScanAbortedException("Aborted");
            }
            fireScanning(vfsEntry);
        }
        
        public void malwareFound(MalwareFoundException malwareFoundException) {
            fireMalwareFound(malwareFoundException);
        }
        
        public void finishedScan() {}

        public void exceptionThrown(Exception exception) {
            fireExceptionThrown(exception);
        }
    };
    
    private final ScanEngine engine;
    
    public ScannerThread(ScanConfiguration scanConf)
    throws CredoException, IOException {
        engine = new ScanEngine(scanConf);
        engine.addScanListener(scanListener);
    }
    
    public void run() {
        scanning = false;
        while (true) {
            synchronized (this) {
                if (!scanning) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                        // that's how it works
                    }
                }
                scanning       = true;
                abortRequested = false;
            } 
            fireStartingScan();
            for (int i = 0; i < scanTargetList.getSize(); i++) {
                final ScanTarget scanTarget =
                        (ScanTarget) scanTargetList.getElementAt(i);
                final File scanFile = scanTarget.getPath();
                try {
                    engine.scan(scanFile);
                } catch (MalwareFoundException mfe) {
                    fireMalwareFound(mfe);
                } catch (Exception e) {
                    fireExceptionThrown(e);
                }
            }
            synchronized (this) {
                scanning = false;
            }
            fireFinishedScan();
        }
    }
    
    /** starts the scanning */
    public synchronized void startScanning() {
        if (scanning == false) {
            notifyAll();
        }
    }
    
    /** notifies, that the scanning should be stopped */
    public synchronized void stopScanning() {
        abortRequested = true;
    }
    
    /** Registers ScannerListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addScanListener(ScanListener listener) {
        if (scanListenerList == null ) {
            scanListenerList = new ArrayList();
        }
        scanListenerList.add(listener);
    }
    
    /** Removes ScannerListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeScanListener(ScanListener listener) {
        if (scanListenerList != null ) {
            scanListenerList.remove(listener);
        }
    }
    
    /** Setter for property scanTargetList.
     * @param scanTargetList New value of property scanTargetList.
     */
    public void setScanTargetList(ScanTargetList scanTargetList) {
        this.scanTargetList = scanTargetList;
    }
    
    /** Getter for property scanning.
     * @return Value of property scanning.
     */
    public synchronized boolean isScanning() {
        return this.scanning;
    }
    
    /** notifies the listeners, that scanning has started */
    protected synchronized void fireStartingScan() {
        if (scanListenerList != null) {
            for (Iterator it = scanListenerList.iterator();
                 it.hasNext(); ) {
                ((ScanListener) it.next()).startingScan();
            }
        }
    }
    
    /** notifies the listeners, that scanning has finished */
    protected synchronized void fireFinishedScan() {
        if (scanListenerList != null) {
            for (Iterator it = scanListenerList.iterator();
                 it.hasNext(); ) {
                ((ScanListener) it.next()).finishedScan();
            }
        }
    }
    
    /** notifies the listeners, that a virus has been found */
    protected synchronized void fireMalwareFound(MalwareFoundException mfe) {
        if (scanListenerList != null) {
            for (Iterator it = scanListenerList.iterator();
                 it.hasNext(); ) {
                ((ScanListener) it.next()).malwareFound(mfe);
            }
        }
    }
    
    /** notifies the listeners, that a file is about to be scanned */
    protected synchronized void fireScanning(VfsEntry entry)
    throws ScanAbortedException {
        if (scanListenerList != null) {
            for (Iterator it = scanListenerList.iterator();
                 it.hasNext(); ) {
                ((ScanListener) it.next()).scanning(entry);
            }
        }
    }
    
    /** notifies the listeners, that an exception has been thrown */
    protected synchronized void fireExceptionThrown(Exception exception) {
        if (scanListenerList != null) {
            for (Iterator it = scanListenerList.iterator();
                 it.hasNext(); ) {
                ((ScanListener) it.next()).exceptionThrown(exception);
            }
        }
    }
}
