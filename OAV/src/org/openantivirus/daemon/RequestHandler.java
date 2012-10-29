/*
 * $Id: RequestHandler.java,v 1.4 2005/09/03 16:16:40 kurti Exp $
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
import java.util.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.vfs.*;

/**
 * ScannerThread
 *
 * Handles a single scan request
 *
 * Pattern-Roles:
 * @author  Kurt Huwig
 * @version $Revision: 1.4 $
 */
public class RequestHandler implements Runnable {
    public static final String VERSION =
        "$Id: RequestHandler.java,v 1.4 2005/09/03 16:16:40 kurti Exp $";
    
    public static final String OK    = "OK\n",
                               ERROR = "ERROR\n";

    
    /** timeout for data connections in milliseconds; use whole seconds */
//    private static final int DATA_TIMEOUT = 10000;
    
    private final Socket socket;
    private final ScanConfiguration scanConfiguration;
    private final ScanEngine scanner;

    private boolean exceptionOccurred;
    
    private final ScanListener scanListener = new ScanListener() {
        public void startingScan() {
        }
        public void scanning(VfsEntry vfsEntry) throws ScanAbortedException {
        }
        public void malwareFound(MalwareFoundException malwareFoundException) {
        }
        public void exceptionThrown(Exception exception) {
            exceptionOccurred = true;
            exception.printStackTrace();
        }
        public void finishedScan() {
        }
    };
    
    public RequestHandler(Socket socket,
                          ScanEngine scanner,
                          ScanConfiguration scanConfiguration) {
        this.socket            = socket;
        this.scanner           = scanner;
        this.scanConfiguration = scanConfiguration;
    }
    
    public void run() {
        String sResult = ERROR;
        BufferedReader br = null;
        PrintWriter    pw = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream());
            
            final String commandLine = br.readLine();
            
            if (commandLine.length() == 0) {
                System.err.println("No command found");
                return;
            }
            
            final int spacePos = commandLine.indexOf(' ');
            String sCommand;
            String argument;
            if (spacePos != -1) {
                sCommand = commandLine.substring(0, spacePos).toUpperCase();
                argument = commandLine.substring(spacePos + 1);
            } else {
                sCommand = commandLine;
                argument = "";
            }
            
            try {
                if (sCommand.equals("SCAN")) {
                    if (argument.length() == 0) {
                        System.err.println("Filename not found");
                        return;
                    }
                    scanner.addScanListener(scanListener);
                    exceptionOccurred = false;
                    scanner.scan(new File(argument), scanConfiguration);
                    if (!exceptionOccurred) {
                        sResult = OK;
                    }
/*                    
                } else if (sCommand.equals("POST")) {
                    ServerSocket dataInServerSocket = new ServerSocket(
                            0, 50,
                            InetAddress.getByName(ScannerDaemon.BINDNAME));
                    
                    dataInServerSocket.setSoTimeout(DATA_TIMEOUT);
                    pw.println("Send data to port '"
                               + dataInServerSocket.getLocalPort()
                               + "' within " + (DATA_TIMEOUT / 1000)
                               + " seconds.");
                    pw.flush();
                    Socket dataInSocket = null;
                    try {
                        dataInSocket  = dataInServerSocket.accept();
                        pw.println("Connected.");
                        pw.flush();
                        new SequentialStreamFilter(
                                scanConfiguration.getTrie()).filter(
                                        dataInSocket.getInputStream(), null);
                        sResult = OK;
                        
                    } catch (InterruptedIOException iie) {
                        // Client failed to connect to data port
                        sResult = ERROR;
                    } finally {
                        if (dataInSocket != null) {
                            dataInSocket.close();
                        }
                        dataInServerSocket.close();
                    }
                    
                } else if (sCommand.equals("FILTER")) {
                    ServerSocket dataInServerSocket = new ServerSocket(
                            0, 50,
                            InetAddress.getByName(ScannerDaemon.BINDNAME));
                    ServerSocket dataOutServerSocket = new ServerSocket(
                            0, 50,
                            InetAddress.getByName(ScannerDaemon.BINDNAME));
                    
                    dataInServerSocket.setSoTimeout(DATA_TIMEOUT);
                    dataOutServerSocket.setSoTimeout(DATA_TIMEOUT);
                    pw.println("Send/receive data to/from port '"
                               + dataInServerSocket.getLocalPort() + "/"
                               + dataOutServerSocket.getLocalPort()
                               + "' within " + (DATA_TIMEOUT / 1000)
                               + " seconds.");
                    pw.flush();
                    Socket dataInSocket = null, dataOutSocket = null;
                    try {
                        dataInSocket  = dataInServerSocket.accept();
                        dataOutSocket = dataOutServerSocket.accept();
                        pw.println("Connected.");
                        pw.flush();
                        new SequentialStreamFilter(
                                scanConfiguration.getTrie()).filter(
                                        dataInSocket.getInputStream(),
                                        sCommand.equals("FILTER") ?
                                        dataOutSocket.getOutputStream() : null);
                        sResult = OK;
                        
                    } catch (InterruptedIOException iie) {
                        // Client failed to connect to data port
                        sResult = ERROR;
                    } finally {
                        if (dataOutSocket != null) {
                            dataOutSocket.close();
                        }
                        if (dataInSocket != null) {
                            dataInSocket.close();
                        }
                        dataInServerSocket.close();
                        dataOutServerSocket.close();
                    }*/
                } else if (sCommand.equals("COMMAND")
                           && argument.length() > 0) {
                    final StringTokenizer st = new StringTokenizer(argument);
                    argument = st.nextToken().toUpperCase();
                    if (argument.equals("SHUTDOWN")) {
                        System.exit(0);
                        /*
                    } else if (argument.equals("CREDO")) {
                        final String subcommand = st.nextToken().toUpperCase();
                        if (subcommand.equals("RELOAD")) {
                            final ScannerConfiguration newConf =
                                    (ScannerConfiguration)
                                    scanConfiguration.clone();
                            new CredoParser(newConf).load();
                            newConf.getTrie().prepare();
                            scanConfiguration.setConfiguration(newConf);
                            sResult = OK;
                        } else {
                            sResult = ERROR + ": Unknown subcommand '"
                                            + argument + "'.";
                        }*/
                    } else {
                        sResult = ERROR + ": Unknown COMMAND '" + argument
                                        + "'.";
                    }
                } else {
                    sResult  = ERROR + ": Unknown command '" + sCommand + "'.";
                }
                
            } catch (MalwareFoundException mfe) {
                sResult = "FOUND '" + mfe.getName()
                        + "' in '" + mfe.getEntry().getName() + "'\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pw != null ) {
                    pw.write(sResult);
                    pw.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
