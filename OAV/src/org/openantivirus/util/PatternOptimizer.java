/*
 * $Id: PatternOptimizer.java,v 1.6 2004/05/26 20:08:46 kurti Exp $
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
 * Portions created by the Initial Developer are Copyright (C) 2001-2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package org.openantivirus.util;

import java.io.*;
import java.util.*;

import org.openantivirus.engine.credo.*;

/**
 * Calculates optimal search patterns
 * 
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.6 $
 */
public class PatternOptimizer {
    
    public PatternOptimizer(String testFile, String patternFile)
    throws Exception {
        final InputStream is = new FileInputStream(testFile);
        final byte[] buffer = new byte[16384];
        
        final int[] count = new int[1 << 16];
        final int[] used  = new int[1 << 16];
        
        System.err.println("Counting...");
        int match = is.read() & 0xff;
        int length;
        while ((length = is.read(buffer)) != -1) {
            
            for (int i = 0; i < length; i++) {
                match = ((match << 8) | (buffer[i] & 0xff)) & 0xffff;
                
                count[match]++;
            }
        }
        
        is.close();
        
        System.err.println("Optimizing...");
        int optimized = 0, patterns = 0, hits = 0;
        
        final BufferedReader br =
            new BufferedReader(new FileReader(patternFile));
        String line;
        while ((line = br.readLine()) != null) {
            patterns++;
            
            final int equalPos = line.indexOf("=");
            class PatternOffset {
                public int offset;
                public String pattern;
                
                public PatternOffset(int offset, String pattern) {
                    this.offset = offset;
                    this.pattern = pattern;
                }
            }
            
            final Collection patternOffsets = new LinkedList();
            for (StringTokenizer st =
                        new StringTokenizer(line.substring(equalPos + 1), "*");
                 st.hasMoreTokens(); ) {
                final String pattern = st.nextToken();
                final WildcardPattern wp = new WildcardPattern(pattern);
                
                
                int min = Integer.MAX_VALUE;
                int minPos = 0;
                int pos = 0;
                int minTriple = 0;
                for (int j = 0; j < wp.skipList.length; j++) {
                    final int skipCount = wp.skipList[j];
                    
                    if ((j & 1) == 0 && skipCount >= 2) {
                        match = wp.pattern[pos] & 0xff;
                        
                        for (int i = 1; i < skipCount; i++) {
                            match = ((match << 8)
                                    | (wp.pattern[i] & 0xff)) & 0xffff;
                            
                            final int tripleCount = count[match];
                            if (tripleCount <= min) {
                                min = tripleCount;
                                minPos = pos + i;
                                minTriple = match;
                            }
                        }
                    }
                    pos += skipCount;
                }
                
                if (min == 0) {
                    optimized++;
                } else {
                    hits += min;
                }
                
                patternOffsets.add(new PatternOffset(minPos, pattern));
                
                used[minTriple] += min;
            }
            
            System.out.print(line.substring(0, equalPos) + "[");
            
            boolean first = true;
            for (Iterator it = patternOffsets.iterator(); it.hasNext(); ) {
                if (first) {
                    first = false;
                } else {
                    System.out.print('*');
                }
                System.out.print(((PatternOffset)it.next()).offset - 1);
            }
            
            System.out.print("]=");
            
            first = true;
            for (Iterator it = patternOffsets.iterator(); it.hasNext(); ) {
                if (first) {
                    first = false;
                } else {
                    System.out.print('*');
                }
                System.out.print(
                        ((PatternOffset)it.next()).pattern.toUpperCase());
            }
            System.out.println();
        }
        br.close();
        
        System.err.println("Optimized: " + optimized + "/" + patterns);
        System.err.println("Hits: " + hits);
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: " + PatternOptimizer.class.getName()
                               + " <test-file> <pattern-db>");
            System.exit(1);
        }
        try {
            new PatternOptimizer(args[0], args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}