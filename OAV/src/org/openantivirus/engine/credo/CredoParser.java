/*
 * $Id: CredoParser.java,v 1.3 2005/09/03 16:16:40 kurti Exp $
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
 * ***** END LICENSE BLOCK ***** */

package org.openantivirus.engine.credo;

import java.io.*;
import java.util.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.censor.*;

/**
 * Reads in Credo-files and initializes the corresponding Finders
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class CredoParser {
    public static final String VERSION =
        "$Id: CredoParser.java,v 1.3 2005/09/03 16:16:40 kurti Exp $";
    
    public static final int NO_VERIFY     = -1;
    public static final int DEFAULT_LEVEL =  3;
    
    private final ScanConfiguration scanConfiguration;
    private final StringsParser stringsParser;
    
    /**
     * @param verify if the digital signature of the Credo-files should be
     *               verified
     */
    public CredoParser(ScanConfiguration scannerConfiguration,
                       StringSearch stringSearch) {
        this.scanConfiguration = scannerConfiguration;
        
        stringsParser = new StringsParser(new StringFinder(stringSearch));
    }
    
    /**
     * Recursively parses all credo files in this directory and subdirectories
     * if this is a directory; otherwise the file itself
     */
    public void parse(File file) throws CredoException, IOException {
        if (file.isDirectory()) {
            final File[] afFiles = file.listFiles(new FilenameFilter() {
                public boolean accept(File directory, String name) {
                    return name.endsWith(CredoFile.EXTENSION);
                }
            });
            for (int i = 0; i < afFiles.length; i++) {
                parse(afFiles[i]);
            }
        } else {
            doParse(new CredoFile(file));
        }
    }
    
    public void parse(InputStream is) throws CredoException, IOException {
        doParse(new CredoFile(is));
    }
    
    protected void doParse(CredoFile credoFile) throws CredoException,
                                                       IOException {
        for (Iterator it = credoFile.entries(); it.hasNext(); ) {
            final CredoEntry credoEntry = (CredoEntry) it.next();
            System.out.println("Reading '" + credoEntry.getJarEntry().getName()
                               + "'...");
            switch (credoEntry.getType()) {
                case CredoEntry.STRINGS:
                    stringsParser.parse(new InputStreamReader(
                            credoEntry.getInputStream()));
                    break;
                default:
                    throw new CredoException("Unknown CredoEntry-type: "
                                             + credoEntry.getType());
            }
            int verifyLevel = scanConfiguration.getInt("credo.level");
            if (verifyLevel != NO_VERIFY) {
                int credoLevel = CredoVerifier.verify(credoEntry);
                if (credoLevel < verifyLevel) {
                    throw new CredoException("Minimum Credo-level "
                            + verifyLevel + " > actual Credo-level "
                            + credoLevel);
                }
                
                System.out.println("  verified Credo-level " + credoLevel);
            }
        }
    }
}
