/*
 * $Id: ScannerDaemon.java,v 1.7 2004/05/31 10:18:44 kurti Exp $
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

package org.openantivirus.daemon;

import java.io.*;
import java.net.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.credo.*;

/**
 * Listens on a port to incoming scan request. Request is of the form
 *
 *  SCAN <filename><newline>
 *
 * Answers with
 *
 *  OK<newline>
 *
 * or
 *
 *  FOUND: <virusname> <virusname> <virusname>...<newline>
 *
 * @author  Kurt Huwig
 * @version $Revision: 1.7 $
 */
public class ScannerDaemon {
    
    public static final String REVISION = "0.6.0";
    
    public static final String BINDNAME = "localhost";
    public static final int PORT = 8127;
    
    private static final String CONF_FILE = "ScannerDaemon-settings.properties";
    private static final int MAX_BACKLOG = 50;
    
    private boolean isRunning;
    
    private final ScanConfiguration scanConf;
    private final ScanEngine engine;
    
    public ScannerDaemon(ScanConfiguration scanConfiguration)
                throws CredoException, IOException {
        engine = new ScanEngine(scanConfiguration);
        this.scanConf = scanConfiguration;
    }
    
    public void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT, MAX_BACKLOG,
                                            InetAddress.getByName(BINDNAME));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }
        
        System.out.println("Listening for incoming requests");
        
        isRunning = true;
        while (isRunning) {
            try {
                final Thread handler = new Thread(new RequestHandler(
                        serverSocket.accept(),
                        engine,
                        scanConf));
                handler.setName("RequestHandler");
                handler.start();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("OpenAntivirus ScannerDaemon v" + REVISION + "\n"
                + "(c) 2001-2004 iKu Systemhaus AG http://www.iku-ag.de/\n"
                + "ScannerDaemon comes with ABSOLUTELY NO WARRANTY; for "
                + "details read 'COPYING'.\n"
                + "This is free software, and you are welcome to redistribute "
                + "it under certain\nconditions; for details read 'COPYING'.");
        
        final WriteableScanConfiguration loadConf =
            new WriteableScanConfiguration(new DefaultScanConfiguration()); 
        final WriteableScanConfiguration scanConf =
            new WriteableScanConfiguration(loadConf);
        
        try {
            boolean confFileLoaded = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].charAt(0) == '-') {
                    final String sParameter = args[i].substring(1);
                    if ("nosignature".equals(sParameter)) {
                        scanConf.putInt("credo.level", CredoParser.NO_VERIFY);
                    } else if ("configfile".equals(sParameter)) {
                        loadConf.loadFile(args[++i]);
                        confFileLoaded = true;
                    } else {
                        scanConf.putAny(sParameter, args[++i]);
                    }
                } else {
                    System.err.println("unknown parameter: " + args[i]);
                }
            }
            
            if (!confFileLoaded) {
                if (new File(CONF_FILE).exists()) { 
                    loadConf.loadFile(CONF_FILE);
                }
            }
            
            new ScannerDaemon(scanConf).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
