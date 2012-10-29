/*
 * $Id: VfsContainer.java,v 1.2 2004/05/23 14:39:19 kurti Exp $
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

package org.openantivirus.engine.vfs;

import java.io.*;

/**
 * A vfs entry that contains other entries
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
public interface VfsContainer {
    /** @return if there are more VfsEntrys in this container */
    boolean hasNext() throws IOException;
    
    /** @return next VfsEntry within this container */
    VfsEntry next() throws IOException;
    
    /**
     * called if the VfsContainer is not needed any more; no method may be
     * called afterwards.
     */
    void dispose() throws IOException;
}
