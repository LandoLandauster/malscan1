package org.openantivirus.util;

import java.io.*;
import java.util.*;

import org.openantivirus.ole.*;

/**
 * Tries to delete as much as possible of a file so that the virus-scanner
 * still detects the virus
 *
 * @author  Kurt Huwig
 * @version $Id: PatternFinder.java,v 1.18 2005/09/03 16:16:41 kurti Exp $
 */
public class PatternFinder {
    
    private final static String INFECTED_SUFFIX = ".infected",
                                CLEARED_SUFFIX  = ".cleared",
                                TEST_FILE       = "test.",
                                WORK_FILE       = "work.",
                                VIRUS_FILE      = "virus.";
    
    private final static int BUFFER_LENGTH  = 16384;
    
    private final byte FILLBYTE1 = (byte) '*', FILLBYTE2 = (byte) '+';
    
    private String sFilename;
    
    private final byte[] abOverwrite;
    
    private String[] asScanArgs;
    
    private Runtime runtime = Runtime.getRuntime();
    
    private int workerCount, skipLines;
    
    private String virusNamePrefix, virusNameSuffix;
    
    public PatternFinder(String sScannerCommand, String sFilename,
                         int workerCount, int skipLines,
                         String virusNamePrefix, String virusNameSuffix) {
        this.sFilename        = sFilename;
        this.workerCount      = workerCount;
        this.skipLines        = skipLines;
        this.virusNamePrefix  = virusNamePrefix;
        this.virusNameSuffix = virusNameSuffix;
        
        StringTokenizer st = new StringTokenizer(sScannerCommand);
        asScanArgs = new String[st.countTokens() + workerCount];
        for (int i = 0; st.hasMoreTokens(); i++) {
            asScanArgs[i] = st.nextToken();
        }
        abOverwrite = new byte[BUFFER_LENGTH];
        
        for (int i = 0; i < abOverwrite.length; i++) {
            abOverwrite[i] = FILLBYTE1;
        }
    }
    
    private class WorkerData {
        /** position within the file */
        public long position;
        
        /** position of the last byte of the area + 1 */
        public long areaEnd;
        
        /** current size to be cleared */
        public long clearSize = 1;
        
        public WorkerData(long position, long areaEnd) {
            this.position = position;
            this.areaEnd  = areaEnd;
        }
    }
    
    private WorkerData[] workerData;
    private String[] workFileNames, testFileNames;
    private Map knownVirii = new HashMap();
    private Map newVirii   = new HashMap();
    private int virusCount = 0;
    
    protected void parallelFind() throws IOException {
        System.err.println("Clearing " + sFilename + "...");
        
        workFileNames = new String[workerCount];
        testFileNames = new String[workerCount];
        
        final String[] asOneScanArgs = new String[asScanArgs.length
                                                  - workerCount + 1];
        for (int i = 0; i < asOneScanArgs.length - 1; i++) {
            asOneScanArgs[i] = asScanArgs[i];
        }
        testFileNames[0] = asOneScanArgs[asOneScanArgs.length - 1] = sFilename;
        
        boolean[] abInfected = checkFiles(asOneScanArgs, 1, null);
        
        if( newVirii.size() == 0 ) {
            System.err.println( "ERROR: File is not infected" );
            System.exit(1);
        }
        
        int[] aiMacroPageIndices = null;
        long lClearLength = -1;
        try {
            OLEReader oleReader = new OLEReader(sFilename);
            aiMacroPageIndices = oleReader.getPageIndices();
            if (aiMacroPageIndices.length > 0) {
                lClearLength = oleReader.getMacroSize();
                System.err.println("File is an OLE document");
            } else {
                System.err.println("Cannot open macro");
                System.exit(1);
            }
        } catch (IOException ioe) {
            // file is not an OLE file, good
        }
        
        while (newVirii.size() > 0) {
            final String virusName =
                    (String) newVirii.keySet().iterator().next();
            sFilename = (String) newVirii.get(virusName);
            knownVirii.put(virusName, sFilename);
            newVirii.remove(virusName);
            
            final Vector infections = new Vector();
            
            for (int worker = 0; worker < workerCount; worker++) {
                workFileNames[worker] = WORK_FILE + (char) ('A' + worker);
                testFileNames[worker] = TEST_FILE + (char) ('A' + worker);
                
                asScanArgs[asScanArgs.length - workerCount + worker] =
                        testFileNames[worker];
            }
            
            createWorkerFiles();
            final long fileLength = createWorker(lClearLength);
            long doneLength = 0;
            
            if (fileLength < workerCount) {
                System.err.println("File too short; reduce number of workers");
                continue;
            }
            
            System.err.print(
                    "0...10...20...30...40...50...60...70...80...90..100%\r");
            System.err.flush();
            
            double dPercent   = fileLength / 50.0;
            long lNextPercent = (long) dPercent;
            int iCurrentPercent = 0;
            do {
                createTestFiles(aiMacroPageIndices);
                abInfected = checkFiles(asScanArgs, workerCount, virusName);
                for (int worker = 0; worker < workerCount; worker++) {
                    final WorkerData data = workerData[worker];
                    if (abInfected[worker]) {
                        data.position  += data.clearSize;
                        doneLength     += data.clearSize;
                        data.clearSize *= 2;
                        if (data.position + data.clearSize >= data.areaEnd) {
                            data.clearSize = data.areaEnd - data.position;
                            // clearSize == 0 -> worker finished
                        }
                        File workFile = new File(workFileNames[worker]);
                        workFile.delete();
                        new File(testFileNames[worker]).renameTo(workFile);
                    } else {
                        if (data.clearSize == 1) {
                            long position;
                            if (aiMacroPageIndices != null) {
                                final int iPageNr    = (int) data.position
                                                             / OLEPage.SIZE,
                                          iPosInPage = (int) data.position
                                                             % OLEPage.SIZE;
                                
                                // we have to stay in the page!
                                if (iPosInPage + data.clearSize
                                        > OLEPage.SIZE) {
                                    data.clearSize = OLEPage.SIZE - iPosInPage;
                                }
                                position = aiMacroPageIndices[iPageNr]
                                           * OLEPage.SIZE + iPosInPage;
                            } else {
                                position = data.position;
                            }
                            infections.add(new Long(position));
                            data.position++;
                            doneLength++;
                            if (data.position == data.areaEnd) {
                                data.clearSize = 0;
                            }
                        } else {
                            data.clearSize /= 2;
                        }
                    }
                }
                
                if (doneLength >= lNextPercent) {
                    while (doneLength >= lNextPercent) {
                        System.err.print('=');
                        System.err.flush();
                        lNextPercent = (long) (dPercent * ++iCurrentPercent);
                    }
                }
            } while (checkWorker());
            System.err.println(' ');
            
            if (infections.size() > 0 ) {
                InputStream isVirus = null;
                OutputStream osInfected = null;
                OutputStream osCleared  = null;
                try {
                    isVirus = new FileInputStream(sFilename);
                    osInfected = new FileOutputStream(sFilename
                                                      + INFECTED_SUFFIX);
                    osCleared = new FileOutputStream(sFilename
                                                     + CLEARED_SUFFIX);
                    printInfections(virusName, infections, fileLength,
                                    isVirus, osInfected, osCleared);
                } finally {
                    if (isVirus != null) {
                        isVirus.close();
                    }
                    if (osInfected != null) {
                        osInfected.close();
                    }
                    if (osCleared != null) {
                        osCleared.close();
                    }
                }
            }
            cleanUp();
        }
    }
    
    /**
     * creates the files for all workers
     */
    protected void createWorkerFiles() throws IOException {
        OutputStream[] aOs = new OutputStream[workerCount];
        InputStream is = new FileInputStream(sFilename);
        try {
            for (int i = 0; i < workerCount; i++) {
                aOs[i] = new FileOutputStream(workFileNames[i]);
            }
            
            byte[] abBuffer = new byte[BUFFER_LENGTH];
            int iLength;
            while ((iLength = is.read(abBuffer)) != -1) {
                for (int i = 0; i < workerCount; i++) {
                    aOs[i].write(abBuffer, 0, iLength);
                }
            }
        } finally {
            for (int i = 0; i < workerCount; i++) {
                if (aOs[i] != null) {
                    aOs[i].close();
                }
            }
            is.close();
        }
    }
    
    /**
     * creates all workers with evenly distributed areas
     *
     * @return filesize
     */
    protected long createWorker(long lClearLength) {
        final long result = (lClearLength == -1) ? new File(sFilename).length()
                                                 : lClearLength;
        
        workerData = new WorkerData[workerCount];
        long lStart = 0;
        for (int i = 0; i < workerCount; i++) {
            long lEnd = result * (i + 1) / workerCount;
            workerData[i] = new WorkerData(lStart, lEnd);
            lStart = lEnd;
        }
        return result;
    }
    
    /**
     * creates cleared files for virus testing
     */
    protected void createTestFiles(int [] aiMacroPageIndices) throws IOException {
        for (int worker = 0; worker < workerCount; worker++) {
            final WorkerData data = workerData[worker];
            long position;
            if (aiMacroPageIndices != null) {
                final int iPageNr    = (int) data.position / OLEPage.SIZE,
                iPosInPage = (int) data.position % OLEPage.SIZE;
                
                // we have to stay in the page!
                if (iPosInPage + data.clearSize > OLEPage.SIZE) {
                    data.clearSize = OLEPage.SIZE - iPosInPage;
                }
                position = aiMacroPageIndices[iPageNr] * OLEPage.SIZE
                           + iPosInPage;
            } else {
                position = data.position;
            }
            
            clearArea(position, position + data.clearSize,
                      workFileNames[worker], testFileNames[worker]);
        }
    }

    /** copies the file while clearing a range of it */
    private void clearArea(long lStart, long lEnd, String clearFile,
                           String testfile) throws IOException {
        InputStream is = new FileInputStream(clearFile);
        try {
            OutputStream os = new FileOutputStream(testfile);
            try {
                byte[] ab = new byte[BUFFER_LENGTH];
                long lPos = 0;
                int iLength;
                
                while (lPos < lStart
                       && (iLength = is.read(ab, 0, Math.min(BUFFER_LENGTH,
                                                             (int) (lStart - lPos))))
                          != -1) {
                    os.write(ab, 0, iLength);
                    lPos += iLength;
                }
                
                while (lPos < lEnd
                       && (iLength = is.read(ab, 0, Math.min(BUFFER_LENGTH,
                                                             (int) (lEnd - lPos))))
                          != -1) {
                    boolean bArrayChanged = false;
                    for (int i = Math.min(BUFFER_LENGTH - 1, (int) (lEnd - lPos)) - 1;
                         i >= 0; i--) {
                        if (ab[i] == FILLBYTE1) {
                            abOverwrite[i] = FILLBYTE2;
                            bArrayChanged = true;
                        }
                    }
                    os.write(abOverwrite, 0, iLength);
                    if (bArrayChanged) {
                        for (int i = Math.min(BUFFER_LENGTH - 1, (int) (lEnd - lPos)) - 1;
                             i >= 0; i--) {
                            abOverwrite[i] = FILLBYTE1;
                        }
                        bArrayChanged = false;
                    }
                    lPos += iLength;
                }
                
                while ((iLength = is.read(ab)) != -1) {
                    os.write(ab, 0, iLength);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }
    
    /**
     * checks files for viruses
     *
     * @return array containing the infection-status of the file
     */
    protected boolean[] checkFiles(String[] scanArgs, int checkWorkerCount,
                                   String virusName) {
        try {
            final Process p = runtime.exec(scanArgs);
            p.waitFor();
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            boolean[] result;
            try {
                result = new boolean[checkWorkerCount];
                String sLine;
                while ((sLine = br.readLine()) != null) {
                    for (int worker = 0; worker < checkWorkerCount; worker++) {
                        if (sLine.indexOf(testFileNames[worker]) != -1) {
                            for (int line = 0; line < skipLines; line++) {
                                sLine = br.readLine();
                            }
                            int iStartPos = sLine.indexOf(virusNamePrefix);
                            int iEndPos   = sLine.lastIndexOf(virusNameSuffix);
                            if (iStartPos != -1 && iEndPos != -1) {
                                final String newVirusName = sLine.substring(
                                        iStartPos + virusNameSuffix.length(),
                                        iEndPos);
                                if (newVirusName.equals(virusName)) {
                                    result[worker] = true;
                                } else {
                                    if (newVirii.get(newVirusName) == null
                                            && knownVirii.get(newVirusName)
                                               == null) {
                                        final String virusFileName =
                                                VIRUS_FILE + (virusCount++);
                                        newVirii.put(newVirusName, virusFileName);
                                        copyFile(testFileNames[worker],
                                                 virusFileName);
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                br.close();
            }
            p.getOutputStream().close();
            p.getErrorStream().close();
            return result;
            
        } catch( Exception e ) {
            e.printStackTrace();
            return new boolean[0];
        }
    }
    
    /**
     * adjusts the boundaries of all workers an reassigns areas to unused workers
     *
     * @return true, as long as workers are running
     */
    protected boolean checkWorker() {
        boolean result = false;
        for (int worker = 0; worker < workerCount; worker++) {
            final WorkerData data = workerData[worker];
            if (data.clearSize == 0) {
                long lMaxSize   = 0;
                int  iMaxWorker = -1;
                for (int i = 0; i < workerCount; i++) {
                    final long lAreaSize = workerData[i].areaEnd
                                           - workerData[i].position;
                    if (lAreaSize > lMaxSize) {
                        lMaxSize   = lAreaSize;
                        iMaxWorker = i;
                    }
                }
                if (iMaxWorker != -1) {
                    final WorkerData maxWorker = workerData[iMaxWorker];
                    final long lMiddle = (maxWorker.position
                                          + maxWorker.areaEnd) / 2;
                    
                    data.position  = lMiddle;
                    data.areaEnd   = maxWorker.areaEnd;
                    data.clearSize = 1;
                    
                    maxWorker.areaEnd = lMiddle;
                    if (maxWorker.position + maxWorker.clearSize > lMiddle) {
                        maxWorker.clearSize = lMiddle - maxWorker.position;
                    }
                }
            } else {
                result = true;
            }
        }
        return result;
    }
    
    protected void printInfections(String virusName, Vector infections,
                                   long fileLength, InputStream isVirus,
                                   OutputStream osInfected,
                                   OutputStream osCleared) throws IOException {
        System.out.print(virusName + "=");
        Object oa[] = infections.toArray();
        Arrays.sort(oa);
        
        long lStart, lEnd, lOldEnd = -1;
        lStart = lEnd = ((Long) oa[0]).longValue();
        for (int i = 1; i < oa.length; i++) {
            final long lPosition = ((Long) oa[i]).longValue();
            if (lPosition != lEnd + 1) {
                if (lStart != 0) {
                    printNotInfected(lOldEnd + 1, lStart - 1,
                                     isVirus, osInfected, osCleared);
                }
                printInfection(lStart, lEnd, isVirus, osInfected, osCleared);
                System.out.print(' ');
                lOldEnd = lEnd;
                lStart = lEnd = lPosition;
            } else {
                lEnd = lPosition;
            }
        }
        printNotInfected(lOldEnd + 1, lStart - 1,
                         isVirus, osInfected, osCleared);
        printInfection(lStart, lEnd, isVirus, osInfected, osCleared);
        printNotInfected(lEnd + 1, fileLength - 1,
                         isVirus, osInfected, osCleared);
        System.out.println();
    }
    
    protected void printInfection(long lStart, long lEnd, InputStream isVirus,
                                  OutputStream osInfected,
                                  OutputStream osCleared) throws IOException {
        //      System.out.println("Infected: " + lStart + "-" + lEnd);
        codedump(lStart, lEnd);
        
        byte[] abBuffer = new byte[BUFFER_LENGTH];
        int infectionLength = (int) (lEnd - lStart + 1);
        while (infectionLength > 0) {
            int iLength = isVirus.read(abBuffer, 0, Math.min(infectionLength,
                                       BUFFER_LENGTH));
            osInfected.write(abBuffer, 0, iLength);
            osCleared.write(abOverwrite, 0, iLength);
            infectionLength -= iLength;
        }
    }
    
    protected void printNotInfected(long lStart, long lEnd, InputStream isVirus,
                                    OutputStream osInfected,
                                    OutputStream osCleared) throws IOException {
        //      System.out.println("Not infected: " + lStart + "-" + lEnd);
        byte[] abBuffer = new byte[BUFFER_LENGTH];
        int clearLength = (int) (lEnd - lStart + 1);
        while (clearLength > 0) {
            int iLength = isVirus.read(abBuffer, 0, Math.min(clearLength,
                                                             BUFFER_LENGTH));
            osInfected.write(abOverwrite, 0, iLength);
            osCleared.write(abBuffer, 0, iLength);
            clearLength -= iLength;
        }
    }
    
    /** dumps an area of a file as hexcode */
    private void codedump(long lStart, long lEnd) throws IOException {
        RandomAccessFile ras = new RandomAccessFile(sFilename, "r");
        ras.seek(lStart);
        do {
            final int value = ras.read();
            System.out.print(Character.forDigit(value >> 4, 16)
                             + Character.forDigit(value % 0x0f, 16));
        } while (++lStart <= lEnd);
        ras.close();
    }
    
    /** deletes all worker and testfiles */
    protected void cleanUp() {
        for (int worker = 0; worker < workerCount; worker++) {
            new File(workFileNames[worker]).delete();
            new File(testFileNames[worker]).delete();
        }
    }
    
    /** copies a file into another file */
    protected void copyFile(String source, String destination)
                throws IOException {
        final InputStream  is = new FileInputStream(source);
        try {
            final OutputStream os = new FileOutputStream(destination);
            try {
                byte[] abBuffer = new byte[BUFFER_LENGTH];
                int iLength;
                while((iLength = is.read(abBuffer)) != -1) {
                    os.write(abBuffer, 0, iLength);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }
    
    public static void main(String[] asParams) {
        if (asParams.length < 6) {
            System.err.println(
                    "Usage: " + PatternFinder.class.getName()
                    + " <scannercommand> <# of workers> <# of lines to skip> "
                    + "<prefix> <suffix> <filename> [<filename>...]");
            System.exit(1);
        }
        
        try {
            final String scannerCommand = asParams[0];
            final int workerCount       = Integer.parseInt(asParams[1]);
            final int skipLines         = Integer.parseInt(asParams[2]);
            final String prefix         = asParams[3];
            final String suffix         = asParams[4];
            for(int i = 5; i < asParams.length; i++) {
                new PatternFinder(scannerCommand, asParams[i], workerCount,
                                  skipLines, prefix, suffix).parallelFind();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
