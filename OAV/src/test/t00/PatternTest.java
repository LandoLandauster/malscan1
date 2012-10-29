/*
 * $Id: PatternTest.java,v 1.2 2005/09/03 16:16:41 kurti Exp $
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

package test.t00;

import java.io.*;

import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.censor.matcharray.*;
import org.openantivirus.engine.censor.trie.*;
import org.openantivirus.engine.credo.*;
import org.openantivirus.engine.vfs.entry.*;

import test.*;

/**
 * Tests different patterns against test files created in a way that the
 * patterns are on buffer boundaries.
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public class PatternTest extends JUnitTest {
    
    public void testPlain() throws Exception {
        testPlainPattern("000102030405", 0);
        testPlainPattern("000102030405", 2);
        testPlainPattern("000102030405", 3);
    }
    
    public void testWildcard() throws Exception {
        testWildcardPattern("000102??0405", 0);
        testWildcardPattern("000102????05", 0);
        testWildcardPattern("00??02030405", 2);
        testWildcardPattern("00??0203??05", 2);
        testWildcardPattern("????02030405", 2);
        testWildcardPattern("????02030405", 3);
        testWildcardPattern("??01??030405", 3);
    }
    
    public void testMultipart() throws Exception {
        testMultipartPattern("0001*02030405", "0*0");
        testMultipartPattern("0001*02030405", "0*1");
        testMultipartPattern("0001*02030405", "0*2");
        testMultipartPattern("000102*030405", "0*0");
        testMultipartPattern("000102*030405", "0*1");
        testMultipartPattern("000102*030405", "1*0");
        testMultipartPattern("000102*030405", "1*1");
        testMultipartPattern("00010203*0405", "0*0");
        testMultipartPattern("00010203*0405", "1*0");
        testMultipartPattern("00010203*0405", "2*0");
        testMultipartPattern("0001*0203*0405", "0*0*0");
        testMultipartPattern("0001*0405", "0*0");
    }
    
    private void testPlainPattern(String pattern, int offset)
    throws Exception {
        final StringSearch stringSearch = createStringSearch();
        
        new StringFinder(stringSearch).addString(pattern,
                                                 offset,
                                                 new PositionFoundListener() {
            public void positionFound(PositionFoundEvent pfe)
                    throws MalwareFoundException {
                throw new MalwareFoundException("test-pattern", null);
            }
        });

        runPatternSearch(stringSearch);
    }
    
    private void testWildcardPattern(String pattern, int offset)
    throws Exception {
        final StringSearch stringSearch = createStringSearch();
        new StringFinder(stringSearch).addWildcardString(
                new WildcardPattern(pattern),
                offset,
                new PositionFoundListener() {
                    public void positionFound(PositionFoundEvent pfe)
                            throws MalwareFoundException {
                        throw new MalwareFoundException("test-pattern", null);
                    }
                });

        runPatternSearch(stringSearch);
    }
    
    private void testMultipartPattern(String pattern, String offsets)
    throws Exception {
        final StringSearch stringSearch = createStringSearch();
        new StringFinder(stringSearch).addMultipartString(
                pattern, offsets, new PositionFoundListener() {
            public void positionFound(PositionFoundEvent pfe)
                    throws MalwareFoundException {
                throw new MalwareFoundException("test-pattern", null);
            }
        });

        runPatternSearch(stringSearch);
    }
    
    private StringSearch createStringSearch() {
        final StringSearch stringSearch;
        if (false) {
            stringSearch = new Trie();
        } else {
            stringSearch = new MatchArray();
        }
        return stringSearch;
    }

    protected void runPatternSearch(StringSearch stringSearch)
    throws Exception {
        stringSearch.prepare();
        final Censor censor = stringSearch.createCensor();
        final byte[][] testPatterns = new byte[][] {
                {0,1,2,3,4,5},
                {3,4,5}
        };
        
        scanTest(censor, 0, testPatterns[0], 0, true);
        scanTest(censor, 2, testPatterns[0], 0, true);
        scanTest(censor, 0, testPatterns[0], 2, true);
        scanTest(censor, 2, testPatterns[0], 2, true);
        scanTest(censor, 32762, testPatterns[0], 20, true);
        scanTest(censor, 32766, testPatterns[0], 20, true);
        scanTest(censor, 32768, testPatterns[0], 20, true);
        
        scanTest(censor, 0, testPatterns[1], 20, false);
    }

    protected void scanTest(Censor censor,
                            int prefix,
                            byte[] data,
                            int suffix,
                            boolean mustBeFound)
    throws ScanException, IOException {
        final File scanFile = createTestfile(prefix, data, suffix);
        try {
            censor.censor(new FileVfsEntry(scanFile));
            
            if (mustBeFound) {
                fail("virus not found");
            }
            
        } catch (MalwareFoundException e) {
            if (!mustBeFound) {
                fail("virus found");
            }
            
        } finally {
            scanFile.delete();
        }
        
    }
    
    protected File createTestfile(int prefix, byte[] data, int suffix)
    throws IOException {
        final File result = File.createTempFile("oav-test", ".bin");
        
        final OutputStream os = new FileOutputStream(result);
        os.write(new byte[prefix]);
        os.write(data);
        os.write(new byte[suffix]);
        os.close();
        
        return result;
    }
    
    public static void main(String[] args) {
        new PatternTest().start();
    }
}
