/*
 * $Id: DefaultScanConfiguration.java,v 1.5 2004/05/30 00:52:44 kurti Exp $
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

package org.openantivirus.engine;

/**
 * The default configuration used by the scanner
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.5 $
 */
public class DefaultScanConfiguration extends WriteableScanConfiguration {

    public DefaultScanConfiguration() {
        putBoolean("engine.halt-on-malware-found", true);
        putBoolean("engine.use-trie", false);
        putBoolean("vfs.follow-symlinks", false);
        putBoolean("vfs.archive.enable", true);
        putBoolean("vfs.archive.ace", true);
        putBoolean("vfs.archive.ar", true);
        putBoolean("vfs.archive.arc", true);
        putBoolean("vfs.archive.arj", true);
        putBoolean("vfs.archive.bzip2", true);
        putBoolean("vfs.archive.cab", true);
        putBoolean("vfs.archive.compress", true);
        putBoolean("vfs.archive.cpio", true);
        putBoolean("vfs.archive.dact", true);
        putBoolean("vfs.archive.dar", true);
        putBoolean("vfs.archive.gzip", true);
        putBoolean("vfs.archive.lha", true);
        putBoolean("vfs.archive.lzo", true);
        putBoolean("vfs.archive.ppmd", true);
        putBoolean("vfs.archive.rar", true);
        putBoolean("vfs.archive.rpm", true);
        putBoolean("vfs.archive.shar", true);
        putBoolean("vfs.archive.tar", true);
        putBoolean("vfs.archive.tnef", true);
        putBoolean("vfs.archive.upx", true);
        putBoolean("vfs.archive.uuencode", true);
        putBoolean("vfs.archive.zip", true);
        putBoolean("vfs.archive.zoo", true);
        
        putString("credo.directory", "credo");
        putString("engine.tempdirectory", "temp");
        
        putInt("credo.level", 3);
    }
}
