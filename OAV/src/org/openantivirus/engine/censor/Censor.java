/*
 * $Id: Censor.java,v 1.1 2003/12/14 11:08:26 kurti Exp $
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

package org.openantivirus.engine.censor;

import org.openantivirus.engine.vfs.*;

/**
 * Takes a file and gives an opinion about it
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.1 $
 */
public interface Censor {
    /**
     * This means that this censor has not found anything suspicious
     */
    int NOTHING_FOUND  = 1;
    
    /** 
     * This means that the given entry is not infected. Make sure that you
     * are correct before returning this value!
     */
    int ENTRY_CLEAN    = 2;
    
    int censor(VfsEntry entry) throws MalwareFoundException, ScanException;
}
