/*
 * $Id: CompressedContainerFactory.java,v 1.6 2005/09/03 16:16:40 kurti Exp $
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

package org.openantivirus.engine.vfs.container;

import java.io.*;
import java.util.zip.*;

import org.openantivirus.engine.*;
import org.openantivirus.engine.vfs.*;
import org.openantivirus.engine.vfs.container.ucl.*;

/**
 * Detects different compression formats and returns the appropriate container
 *
 * Pattern-Roles: Factory
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.6 $
 */
public class CompressedContainerFactory implements VfsContainerFactory {
    private final byte[]
            ACE_MAGIC   = "**ACE**".getBytes(),
            AR_MAGIC    = "!<arch>".getBytes(),
            ARC2_MAGIC  = {(byte)0x1a, (byte)0x02},
            ARC3_MAGIC  = {(byte)0x1a, (byte)0x03},
            ARC4_MAGIC  = {(byte)0x1a, (byte)0x04},
            ARC6_MAGIC  = {(byte)0x1a, (byte)0x06},
            ARC8_MAGIC  = {(byte)0x1a, (byte)0x08},
            ARC9_MAGIC  = {(byte)0x1a, (byte)0x09},
            ARJ_MAGIC   = {(byte)0x60, (byte)0xea},
            CAB_MAGIC   = {'M', 'S', 'C', 'F', 0, 0, 0, 0},
            CPIO_MAGIC  = {(byte)0xc7, (byte)0x71},
            CPIOS_MAGIC = {(byte)0x71, (byte)0xc7},
            CPIO1_MAGIC = "070701".getBytes(),
            CPIO2_MAGIC = "070702".getBytes(),
            CPIO7_MAGIC = "070707".getBytes(),
            COMPR_MAGIC = {(byte)0x1f, (byte)0x9d},
            DACT_MAGIC  = {(byte)0x44, (byte)0x43, (byte)0x54, (byte)0xc3},
            DAR_MAGIC   = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7b},
            EXE_MAGIC   = "MZ".getBytes(),
            BZIP2_MAGIC = "BZh".getBytes(),
            GZIP_MAGIC  = {(byte)0x1f, (byte)0x8b},
            LHA__MAGIC  = "-lh -".getBytes(),
            LHA0_MAGIC  = "-lh0-".getBytes(),
            LHA1_MAGIC  = "-lh1-".getBytes(),
            LHA2_MAGIC  = "-lh2-".getBytes(),
            LHA3_MAGIC  = "-lh3-".getBytes(),
            LHA4_MAGIC  = "-lh4-".getBytes(),
            LHA5_MAGIC  = "-lh5-".getBytes(),
            LHA6_MAGIC  = "-lh6-".getBytes(),
            LHA7_MAGIC  = "-lh7-".getBytes(),
            LHAD_MAGIC  = "-lhd-".getBytes(),
            LHAZ4_MAGIC = "-lz4-".getBytes(),
            LHAZ5_MAGIC = "-lz5-".getBytes(),
            LZO_MAGIC   = {(byte)0x89, (byte)0x4c, (byte)0x5a, (byte)0x4f,
                           (byte)0x00, (byte)0x0d, (byte)0x0a, (byte)0x1a},
            PPMD_MAGIC  = {(byte)0x8f, (byte)0xaf, (byte)0xac, (byte)0x84},
            RAR_MAGIC   = "Rar!".getBytes(),
            RPM_MAGIC   = {(byte)0xed, (byte)0xab, (byte)0xee, (byte)0xdb},
            SHAR_MAGIC  = "# This is a shell archive".getBytes(),
            TAR_MAGIC   = "ustar".getBytes(),
            TNEF_MAGIC  = {(byte)0x78, (byte)0x9f, (byte)0x3e, (byte)0x22},
            UUENC_MAGIC = "begin ".getBytes(),
            ZIP_MAGIC   = {'P', 'K', 3, 4},
            ZOO_MAGIC   = {(byte)0xdc, (byte)0xa7, (byte)0xc4, (byte)0xfd};
    
    public VfsContainer getContainer(VfsEntry entry,
                                     ScanConfiguration scanConf)
                                     throws IOException {
        
        if (!scanConf.getBoolean("vfs.archive.enable")) {
            return null;
        }
        
        final byte[] start = entry.getStart();
        
        // ZIP
        if (scanConf.getBoolean("vfs.archive.zip")
                && startsWithMagic(start, ZIP_MAGIC)) {
            return new ZipContainer(entry, scanConf);
        }
        
        // BZIP2
        if (scanConf.getBoolean("vfs.archive.bzip2")
                && startsWithMagic(start, BZIP2_MAGIC)) {
            return new SingleFileContainer(entry, "bzip2", scanConf) {
                public void extractFile() throws IOException {
                    runCommand(new String[] {
                                          "bunzip2",
                                          "-c",
                                          entry.getFile().getCanonicalPath()},
                               tempFile.getFile());
                }
            };
        }
        
        // GZIP
        if (scanConf.getBoolean("vfs.archive.gzip")
                && startsWithMagic(start, GZIP_MAGIC)) {
            return new SingleFileContainer(entry, "gzip", scanConf) {
                public void extractFile() throws IOException {
                    copyStream(new GZIPInputStream(
                                   new FileInputStream(entry.getFile())),
                               new FileOutputStream(tempFile.getFile()));
                }
            };
        }
        
        // TAR
        if (scanConf.getBoolean("vfs.archive.tar")
                && containsMagic(start, TAR_MAGIC, 257)) {
            return new ArchiveContainer(entry, " >> tar:", scanConf) {
                public void extractArchive()
                throws IOException {
                    runCommand(new String[] {
                            "tar",
                            "xfC",
                            entry.getFile().getCanonicalPath(),
                            tempDir.getDirectory().getCanonicalPath()});
                }
            };
        }
        
        // Microsoft Cabinet
        if (scanConf.getBoolean("vfs.archive.cab")
                && startsWithMagic(start, CAB_MAGIC)) {
            return new ArchiveContainer(entry, " >> cab:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                            "cabextract",
                            "-d",
                            tempDir.getDirectory().getCanonicalPath(),
                            entry.getFile().getCanonicalPath()});
                }
            };
        }
        
        // Self-extracting
        if (startsWithMagic(start, EXE_MAGIC)) {
            if (scanConf.getBoolean("vfs.archive.rar")
                    && containsMagic(start, RAR_MAGIC, 7195)) {
                return new ArchiveContainer(entry,
                                            " >> rar-exe:",
                                            scanConf) {
                    public void extractArchive() throws IOException {
                        runCommand(new String[] {
                                   "unrar",
                                   "x",
                                   entry.getFile().getCanonicalPath()},
                                   tempDir.getDirectory());
                    }
                };
            }
            
            if (scanConf.getBoolean("vfs.archive.upx")) {
                final File file = entry.getFile();
                final RandomAccessFile raf = new RandomAccessFile(file, "r");
                final UPXDecompress upxDecompress =
                        new UPXDecompress(raf, file.length());
                
                boolean canUnpack;
                try {
                    canUnpack = upxDecompress.canUnpack();
                } catch (Exception e) {
                    // if anything goes wrong, we cannot unpack!
                    e.printStackTrace();
                    canUnpack = false;
                }
                if (canUnpack) {
                    return new UpxContainer(entry, upxDecompress, scanConf);
                }
                raf.close();
            }
        }
        
        // ACE
        if (scanConf.getBoolean("vfs.archive.ace")
                && containsMagic(start, ACE_MAGIC, 7)) {
            return new ArchiveContainer(entry, " >> ace:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "unace",
                               "x",
                               "-y",
                               entry.getFile().getCanonicalPath()},
                               tempDir.getDirectory(),
                               null,
                               false);
                }
            };
        }
        
        // AR
        if (scanConf.getBoolean("vfs.archive.ar")
                && startsWithMagic(start, AR_MAGIC)) {
            return new ArchiveContainer(entry, " >> ar:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "ar",
                               "x",
                               entry.getFile().getCanonicalPath()},
                               tempDir.getDirectory());
                }
            };
        }
        
        // RAR
        if (scanConf.getBoolean("vfs.archive.rar")
                && startsWithMagic(start, RAR_MAGIC)) {
            return new ArchiveContainer(entry, " >> rar:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "unrar",
                               "x",
                               entry.getFile().getCanonicalPath()},
                               tempDir.getDirectory());
                }
            };
        }
        
        // LHA
        if (scanConf.getBoolean("vfs.archive.lha")
                && (containsMagic(start, LHA__MAGIC, 2)
                    || containsMagic(start, LHA0_MAGIC, 2)
                    || containsMagic(start, LHA1_MAGIC, 2)
                    || containsMagic(start, LHA2_MAGIC, 2)
                    || containsMagic(start, LHA3_MAGIC, 2)
                    || containsMagic(start, LHA4_MAGIC, 2)
                    || containsMagic(start, LHA5_MAGIC, 2)
                    || containsMagic(start, LHA6_MAGIC, 2)
                    || containsMagic(start, LHA7_MAGIC, 2)
                    || containsMagic(start, LHA7_MAGIC, 2)
                    || containsMagic(start, LHAD_MAGIC, 2)
                    || containsMagic(start, LHAZ4_MAGIC, 2)
                    || containsMagic(start, LHAZ5_MAGIC, 2))) {
            return new ArchiveContainer(entry, " >> lha:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "lha",
                               "xw="
                               + tempDir.getDirectory().getCanonicalPath(),
                               entry.getFile().getCanonicalPath()});
                }
            };
        }
        
        // ARJ
        if (scanConf.getBoolean("vfs.archive.arj")
                && startsWithMagic(start, ARJ_MAGIC)) {
            return new ArchiveContainer(entry, " >> arj:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "unarj",
                               "x",
                               entry.getFile().getCanonicalPath()},
                               tempDir.getDirectory());
                }
            };
        }
        
        // ZOO
        if (scanConf.getBoolean("vfs.archive.zoo")
                && containsMagic(start, ZOO_MAGIC, 0x14)) {
            return new ArchiveContainer(entry, " >> zoo:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "unzoo",
                               "-x",
                               "-j",
                               tempDir.getDirectory().getCanonicalPath()
                               + File.separator,
                               entry.getFile().getCanonicalPath()});
                }
            };
        }
        
        // CPIO
        if (scanConf.getBoolean("vfs.archive.cpio")
                && (startsWithMagic(start, CPIO_MAGIC)
                    || startsWithMagic(start, CPIOS_MAGIC)
                    || startsWithMagic(start, CPIO1_MAGIC)
                    || startsWithMagic(start, CPIO2_MAGIC)
                    || startsWithMagic(start, CPIO7_MAGIC))) {
            return new ArchiveContainer(entry, " >> cpio:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "cpio",
                               "-i",
                               "-F",
                               entry.getFile().getCanonicalPath()},
                               tempDir.getDirectory());
                }
            };
        }
        
        // TNEF
        if (scanConf.getBoolean("vfs.archive.tnef")
                && startsWithMagic(start, TNEF_MAGIC)) {
            return new ArchiveContainer(entry, " >> tnef:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "tnef",
                               "-f",
                               entry.getFile().getCanonicalPath(),
                               "-C",
                               tempDir.getDirectory().getCanonicalPath()});
                }
            };
        }
        
        // SHAR
        if (scanConf.getBoolean("vfs.archive.shar")
                && containsMagic(start, SHAR_MAGIC, 10)) {
            return new ArchiveContainer(entry, " >> shar:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "unshar",
                               "-d",
                               tempDir.getDirectory().getCanonicalPath(),
                               entry.getFile().getCanonicalPath()});
                }
            };
        }
        
        // UUENCODE
        if (scanConf.getBoolean("vfs.archive.uuencode")
                && startsWithMagic(start, UUENC_MAGIC)) {
            return new SingleFileContainer(entry, "uuencode", scanConf) {
                public void extractFile() throws IOException {
                    runCommand(new String[] {
                                          "uudecode",
                                          "-o",
                                          "/dev/stdout",
                                          entry.getFile().getCanonicalPath()},
                               tempFile.getFile());
                }
            };
        }
        
        // COMPRESS
        if (scanConf.getBoolean("vfs.archive.compress")
                && startsWithMagic(start, COMPR_MAGIC)) {
            return new SingleFileContainer(entry, "compress", scanConf) {
                public void extractFile() throws IOException {
                    runCommand(new String[] {
                                          "uncompress",
                                          "-d",
                                          "-c",
                                          entry.getFile().getCanonicalPath()},
                               tempFile.getFile());
                }
            };
        }
        
        // RPM
        if (scanConf.getBoolean("vfs.archive.rpm")
                && startsWithMagic(start, RPM_MAGIC)) {
            return new ArchiveContainer(entry, " >> rpm:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "rpm2cpio",
                               entry.getFile().getCanonicalPath()},
                               tempDir.getDirectory(),
                               new FileOutputStream(
                                       tempDir.getDirectory().getCanonicalPath()
                                       + File.separator + "rpm.cpio"),
                               false);
                }
            };
        }
        
        // ARC
        if (scanConf.getBoolean("vfs.archive.arc")
                && (startsWithMagic(start, ARC2_MAGIC)
                    || startsWithMagic(start, ARC3_MAGIC)
                    || startsWithMagic(start, ARC4_MAGIC)
                    || startsWithMagic(start, ARC6_MAGIC)
                    || startsWithMagic(start, ARC8_MAGIC)
                    || startsWithMagic(start, ARC9_MAGIC))) {
            return new ArchiveContainer(entry, " >> arc:", scanConf) {
                public void extractArchive() throws IOException {
                    runCommand(new String[] {
                               "arc",
                               "x",
                               entry.getFile().getCanonicalPath()},
                               tempDir.getDirectory());
                }
            };
        }
        
        // LZO
        if (scanConf.getBoolean("vfs.archive.lzo")
                && startsWithMagic(start, LZO_MAGIC)) {
            return new SingleFileContainer(entry, "lzop", scanConf) {
                public void extractFile() throws IOException {
                    runCommand(new String[] {
                                          "lzop",
                                          "-d",
                                          "-c",
                                          entry.getFile().getCanonicalPath()},
                               tempFile.getFile());
                }
            };
        }
        
        // PPMd
        if (scanConf.getBoolean("vfs.archive.ppmd")
                && startsWithMagic(start, PPMD_MAGIC)) {
            return new ArchiveContainer(entry, " >> ppmd:", scanConf) {
                public void extractArchive() throws IOException {
                    final String tempFilename =
                        tempDir.getDirectory().getCanonicalPath()
                        + File.separator
                        + "file.pmd"; 
                    SingleFileContainer.copyStream(
                            new FileInputStream(entry.getFile()),
                            new FileOutputStream(new File(tempFilename)));
                    runCommand(new String[] {
                               "PPMd",
                               "d",
                               "-d",
                               tempFilename},
                               tempDir.getDirectory());
                }
            };
        }
        
        // DACT
        if (scanConf.getBoolean("vfs.archive.dact")
                && startsWithMagic(start, DACT_MAGIC)) {
            return new SingleFileContainer(entry, "dact", scanConf) {
                public void extractFile() throws IOException {
                    runCommand(new String[] {
                                          "dact",
                                          "-d",
                                          "-c",
                                          entry.getFile().getCanonicalPath()},
                               tempFile.getFile());
                }
            };
        }
        
        // DAR
        if (scanConf.getBoolean("vfs.archive.dar")
                && startsWithMagic(start, DAR_MAGIC)) {
            return new ArchiveContainer(entry, " >> dar:", scanConf) {
                public void extractArchive() throws IOException {
                    final String filename = entry.getFile().getCanonicalPath();
                    runCommand(new String[] {
                               "dar",
                               "-x",
                               filename.substring(
                                       0,
                                       filename.lastIndexOf(
                                               '.',
                                               filename.lastIndexOf('.') - 1)),
                               "-O",
                               "-N"},
                               tempDir.getDirectory());
                }
            };
        }
        
        return null;
    }
    
    protected boolean startsWithMagic(byte[] start, byte[] magic) {
        return containsMagic(start, magic, 0);
    }
    
    protected boolean containsMagic(byte[] start, byte[] magic, int offset) {
        if (start.length < magic.length + offset) {
            return false;
        }
        
        for (int i = 0; i < magic.length; i++) {
            if (start[i + offset] != magic[i]) {
                return false;
            }
        }
        
        return true;
    }
}