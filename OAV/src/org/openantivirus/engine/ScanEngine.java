/*
 * $Id: ScanEngine.java,v 1.9 2005/09/03 16:16:40 kurti Exp $
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

package org.openantivirus.engine;

import java.io.*;
import java.util.*;

import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.censor.matcharray.*;
import org.openantivirus.engine.censor.trie.*;
import org.openantivirus.engine.credo.*;
import org.openantivirus.engine.vfs.*;
import org.openantivirus.engine.vfs.container.*;
import org.openantivirus.engine.vfs.entry.*;

/**
 * You need this class to scan files
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.9 $
 */
public class ScanEngine {
    private final VfsContainerFactory[] containerFactories = {
                new DirectoryContainerFactory(),
                new CompressedContainerFactory()};

    private final CensorFactory[] censorFactories = new CensorFactory[1];

    private final List scanListeners = new LinkedList();
    private final ScanConfiguration scanConfiguration;

    public ScanEngine() throws IOException, CredoException {
        this(new DefaultScanConfiguration());
    }
    
    public ScanEngine(ScanConfiguration scanConfiguration)
    throws IOException, CredoException {
        this.scanConfiguration = scanConfiguration;
        
        loadCredoFiles();
    }
    
    public void loadCredoFiles()
    throws CredoException, IOException {
        final StringSearch stringSearch;
        if (scanConfiguration.getBoolean("engine.use-trie")) {
            stringSearch = new Trie();
        } else {
            stringSearch = new MatchArray();
        }
        
        new CredoParser(scanConfiguration, stringSearch).parse(
                new File(scanConfiguration.getString("credo.directory")));
        
        stringSearch.prepare();
        censorFactories[0] = stringSearch;
        System.gc();
    }

    /**
	 * scans the file using the default configuration
	 * 
	 * @throws IOException
	 *             if the file cannot be accessed
	 * @throws MalwareFoundException
	 *             if malware has been found within this file
	 */
    public void scan(File file) throws MalwareFoundException {
        scan(file, scanConfiguration);
    }
 
    /**
	 * scans the file using the given configuration
	 * 
	 * @throws IOException
	 *             if the file cannot be accessed
	 * @throws MalwareFoundException
	 *             if malware has been found within this file
	 */
    public void scan(File file, ScanConfiguration conf)
        throws MalwareFoundException {
        notifyStartingScan();
        try {
            scan(new FileVfsEntry(file), conf);
        } catch (ScanAbortedException sae) {
            // ok, we stop here
        }
        notifyFinishedScan();
    }

    protected void scan(VfsEntry entry, ScanConfiguration conf)
        throws MalwareFoundException, ScanAbortedException {
        try {
            notifyScanning(entry);
            if (scanContainer(entry, conf)) {
                return;
            }

            for (int i = 0; i < censorFactories.length; i++) {
                try {
                    final int result =
                        censorFactories[i].createCensor().censor(entry);
    
                    if (result == Censor.ENTRY_CLEAN) {
                        return;
                    }
                } catch (MalwareFoundException mfe) {
                    notifyMalwareFound(mfe);
                    if (conf.getBoolean(
                            "engine.halt-on-malware-found")) {
                        throw mfe;
                    }
                } catch (Exception e) {
                    // this does not keep us from scanning
                    notifyException(e);
                }
            }
        } finally {
            try {
                entry.dispose();
            } catch (IOException ioe) {
                notifyException(ioe);
            }
        }
    }

    protected boolean scanContainer(VfsEntry entry, ScanConfiguration conf)
        throws MalwareFoundException {
        boolean containerFound = false;
        VfsContainer container = null;
        
        try {
            for (int i = 0;
                 i < containerFactories.length && !containerFound;
                 i++) {
                try {
                    container = containerFactories[i].getContainer(entry, conf);
                    if (container == null) {
                        continue;
                    }
                    
                    containerFound = true;
    
                    while (container.hasNext()) {
                        try {
                            scan(container.next(), conf);
                        } catch (MalwareFoundException mfe) {
                            throw mfe;
                        } catch (Exception e) {
                            // ok, try the next one
                            notifyException(e);
                        }
                    }
                    
                    container.dispose();
                    
                } catch (MalwareFoundException mfe) {
                    throw mfe;
                } catch (Exception e) {
                    notifyException(e);
                }
            }
        } finally {
            if (containerFound && container != null) {
                try {
                    container.dispose();
                } catch (IOException ioe) {
                    notifyException(ioe);
                }
            }
        }
           
        return containerFound;
    }

    public void addScanListener(ScanListener listener) {
        scanListeners.add(listener);
    }
    
    public void removeScanListener(ScanListener listener) {
        scanListeners.remove(listener);
    }
    
    protected void notifyStartingScan() {
        for (Iterator it = scanListeners.iterator(); it.hasNext();) {
            ((ScanListener) it.next()).startingScan();
        }
    }

    protected void notifyScanning(VfsEntry entry) throws ScanAbortedException {
        for (Iterator it = scanListeners.iterator(); it.hasNext();) {
            ((ScanListener) it.next()).scanning(entry);
        }
    }

    protected void notifyException(Exception exception) {
        for (Iterator it = scanListeners.iterator(); it.hasNext();) {
            ((ScanListener) it.next()).exceptionThrown(exception);
        }
    }

    protected void notifyMalwareFound(MalwareFoundException mfe) {
        for (Iterator it = scanListeners.iterator(); it.hasNext();) {
            ((ScanListener) it.next()).malwareFound(mfe);
        }
    }
    
    protected void notifyFinishedScan() {
        for (Iterator it = scanListeners.iterator(); it.hasNext();) {
            ((ScanListener) it.next()).finishedScan();
        }
    }

}
