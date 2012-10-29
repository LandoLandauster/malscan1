/*
 * $Id: ScanListener.java,v 1.2 2004/05/20 13:53:54 kurti Exp $
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

import java.util.*;

import org.openantivirus.engine.censor.*;
import org.openantivirus.engine.vfs.*;

/**
 * Listener for events occuring during scanning
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public interface ScanListener extends EventListener {
    /** called when the scan is about to start */
    void startingScan();
    
    /**
     * called for each files to be scanned
     * @param vfsEntry VfsEntry to be scanned
     */
    void scanning(VfsEntry vfsEntry) throws ScanAbortedException;
    
    /** called, when a malware has been found */
    void malwareFound(MalwareFoundException malwareFoundException);

    /** called when an Exception other than MalwareFoundException was thrown */
    void exceptionThrown(Exception exception);
    
    /** called when the scan has finished */
    void finishedScan();
}
