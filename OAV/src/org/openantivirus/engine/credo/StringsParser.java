/*
 * $Id: StringsParser.java,v 1.7 2004/05/22 12:22:26 kurti Exp $
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

import org.openantivirus.engine.censor.*;

/**
 * Parses '.strings' credo files
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.7 $
 */
public class StringsParser {
    private final StringFinder stringFinder;
    
    public StringsParser(StringFinder stringFinder) {
        this.stringFinder = stringFinder;
    }
    
    public void parse(Reader patternReader)
    throws IOException {

        final BufferedReader br = new BufferedReader(patternReader);
        String sLine;
        while ((sLine = br.readLine()) != null) {
            parseLine(sLine);
        }
        // br may not be closed!
    }
    
    private void parseLine(String line) {
        final int equalsPos = line.indexOf('=');
        if (equalsPos == -1) {
            System.err.println("Malformed pattern line: " + line);
            return;
        }
        
        final int offsetStart = line.lastIndexOf('[');
        final String sPattern = line.substring(equalsPos + 1);
        final PositionFoundListener pfl = new StringFoundListener(
                new String(line.substring(0, offsetStart)));
        
        try {
            if (sPattern.indexOf('*') != -1) {
                stringFinder.addMultipartString(
                        sPattern,
                        line.substring(offsetStart + 1, equalsPos - 1),
                        pfl);
            } else {
                final int offset = Integer.parseInt(
                        line.substring(offsetStart + 1, equalsPos - 1));
                if (sPattern.indexOf('?') != -1) {
                    stringFinder.addWildcardString(
                            new WildcardPattern(sPattern),
                            offset,
                            pfl);
                } else {
                    stringFinder.addString(sPattern, offset, pfl);
                }
            }
        } catch (Exception e) {
            System.err.println("Malformed pattern line: " + line);
            e.printStackTrace();
        }
    }

    private static class StringFoundListener implements PositionFoundListener {
        private final String virusName;
        
        public StringFoundListener(String virusName) {
            this.virusName = virusName;
        }
        
        public void positionFound(PositionFoundEvent pfe)
        throws MalwareFoundException {
            throw new MalwareFoundException(virusName, pfe.entry);
        }
    }
    
}
